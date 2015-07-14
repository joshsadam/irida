package ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.ExecutionManagerException;
import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowAnalysisTypeException;
import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowException;
import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowNotFoundException;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisState;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePairSnapshot;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFileSnapshot;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.execution.galaxy.PreparedWorkflowGalaxy;
import ca.corefacility.bioinformatics.irida.model.workflow.execution.galaxy.WorkflowInputsGalaxy;
import ca.corefacility.bioinformatics.irida.model.workflow.structure.IridaWorkflowStructure;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyWorkflowService;
import ca.corefacility.bioinformatics.irida.service.AnalysisService;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.analysis.annotations.RunAsUser;
import ca.corefacility.bioinformatics.irida.service.analysis.workspace.galaxy.AnalysisWorkspaceServiceGalaxy;
import ca.corefacility.bioinformatics.irida.service.snapshot.SequenceFileSnapshotService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

/**
 * Service for executing {@link AnalysisSubmission} stages within a Galaxy
 * execution manager asynchronously.
 * 
 */
@Async("analysisTaskExecutor")
public class AnalysisExecutionServiceGalaxyAsync {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisExecutionServiceGalaxyAsync.class);

	private final AnalysisSubmissionService analysisSubmissionService;
	private final AnalysisService analysisService;
	private final AnalysisWorkspaceServiceGalaxy workspaceService;
	private final GalaxyWorkflowService galaxyWorkflowService;
	private final IridaWorkflowsService iridaWorkflowsService;
	private final SequenceFileSnapshotService sequenceFileSnapshotService;

	/**
	 * Builds a new {@link AnalysisExecutionServiceGalaxyAsync} with the given
	 * information.
	 * 
	 * @param analysisSubmissionService
	 *            A service for analysis submissions.
	 * @param analysisService
	 *            A service for analysis results.
	 * @param galaxyWorkflowService
	 *            A service for Galaxy workflows.
	 * @param workspaceService
	 *            A service for a workflow workspace.
	 * @param iridaWorkflowsService
	 *            A service for loading up {@link IridaWorkflow}s.
	 * @param sequenceFileSnapshotService
	 *            A service for storing and retrieving local
	 *            {@link SequenceFileSnapshot}s
	 */
	@Autowired
	public AnalysisExecutionServiceGalaxyAsync(AnalysisSubmissionService analysisSubmissionService,
			AnalysisService analysisService, GalaxyWorkflowService galaxyWorkflowService,
			AnalysisWorkspaceServiceGalaxy workspaceService, IridaWorkflowsService iridaWorkflowsService,
			SequenceFileSnapshotService sequenceFileSnapshotService) {
		this.analysisSubmissionService = analysisSubmissionService;
		this.analysisService = analysisService;
		this.galaxyWorkflowService = galaxyWorkflowService;
		this.workspaceService = workspaceService;
		this.iridaWorkflowsService = iridaWorkflowsService;
		this.sequenceFileSnapshotService = sequenceFileSnapshotService;
	}
	
	/**
	 * Download the remote files for an {@link AnalysisSubmission}.
	 * 
	 * @param analysisSubmission
	 *            The {@link AnalysisSubmission} to get files for.
	 * @return A Future {@link AnalysisSubmission} with the files locally
	 *         mirrored
	 */
	@Transactional
	@RunAsUser("#analysisSubmission.getSubmitter()")
	public Future<AnalysisSubmission> downloadFilesForSubmission(final AnalysisSubmission analysisSubmission) {
		checkNotNull(analysisSubmission, "analysisSubmission is null");
		checkNotNull(analysisSubmission.getId(), "analysisSubmission id is null");

		// Get all the remote paired files and save them locally
		Set<SequenceFilePairSnapshot> remoteFilesPaired = analysisSubmission.getRemoteFilesPaired();
		for (SequenceFilePairSnapshot pair : remoteFilesPaired) {
			for (SequenceFileSnapshot file : pair.getFiles()) {
				sequenceFileSnapshotService.mirrorFileContent(file);
			}
		}

		// Get all the individual files and save them locally
		for (SequenceFileSnapshot file : analysisSubmission.getRemoteFilesSingle()) {
			sequenceFileSnapshotService.mirrorFileContent(file);
		}

		// once complete update the state
		AnalysisSubmission analysisPrepared = analysisSubmissionService.update(analysisSubmission.getId(),
				ImmutableMap.of("analysisState", AnalysisState.FINISHED_DOWNLOADING));

		return new AsyncResult<>(analysisPrepared);
	}

	/**
	 * Prepares the given {@link AnalysisSubmission} to be executed within an
	 * execution manager. This will persist the submission within the database.
	 * 
	 * @param analysisSubmission
	 *            The {@link AnalysisSubmission} to prepare.
	 * @return A {@link Future} with an {@link AnalysisSubmission} for the
	 *         analysis submitted.
	 * @throws IridaWorkflowNotFoundException
	 *             If there was an issue getting a workflow.
	 * @throws IOException
	 *             If there was an issue reading the workflow.
	 * @throws ExecutionManagerException
	 *             If there was an issue preparing a workspace for the workflow.
	 */
	@Transactional
	public Future<AnalysisSubmission> prepareSubmission(final AnalysisSubmission analysisSubmission)
			throws IridaWorkflowNotFoundException, IOException, ExecutionManagerException {
		checkNotNull(analysisSubmission, "analysisSubmission is null");
		checkNotNull(analysisSubmission.getId(), "analysisSubmission id is null");
		checkArgument(null == analysisSubmission.getRemoteAnalysisId(), "remote analyis id should be null");
		checkArgument(null == analysisSubmission.getRemoteWorkflowId(), "remoteWorkflowId should be null");

		IridaWorkflow iridaWorkflow = iridaWorkflowsService.getIridaWorkflow(analysisSubmission.getWorkflowId());
		IridaWorkflowStructure workflowStructure = iridaWorkflow.getWorkflowStructure();

		logger.debug("Preparing submission for " + analysisSubmission);

		String workflowId = galaxyWorkflowService.uploadGalaxyWorkflow(workflowStructure.getWorkflowFile());
		analysisSubmission.setRemoteWorkflowId(workflowId);
		logger.trace("Uploaded workflow for " + analysisSubmission + " to workflow with id=" + workflowId);

		String analysisId = workspaceService.prepareAnalysisWorkspace(analysisSubmission);

		logger.trace("Created Galaxy history for analysis " + " id=" + analysisId + ", " + analysisSubmission);

		AnalysisSubmission analysisPrepared = analysisSubmissionService.update(analysisSubmission.getId(), ImmutableMap
				.of("remoteAnalysisId", analysisId, "remoteWorkflowId", workflowId, "analysisState",
						AnalysisState.PREPARED));

		return new AsyncResult<>(analysisPrepared);
	}

	/**
	 * Executes the passed prepared {@link AnalysisSubmission} in an execution
	 * manager.
	 * 
	 * @param analysisSubmission
	 *            The {@link AnalysisSubmission} to execute.
	 * @return A {@link Future} with an {@link AnalysisSubmission} for the
	 *         analysis submitted.
	 * @throws ExecutionManagerException
	 *             If there was an exception submitting the analysis to the
	 *             execution manager.
	 * @throws IridaWorkflowException If there was an issue with the IRIDA workflow.
	 */
	@Transactional
	@RunAsUser("#analysisSubmission.getSubmitter()")
	public Future<AnalysisSubmission> executeAnalysis(AnalysisSubmission analysisSubmission)
			throws ExecutionManagerException, IridaWorkflowException {
		checkNotNull(analysisSubmission, "analysisSubmission is null");
		checkNotNull(analysisSubmission.getRemoteAnalysisId(), "remote analyis id is null");
		checkNotNull(analysisSubmission.getWorkflowId(), "workflowId is null");

		logger.debug("Running submission for " + analysisSubmission);

		logger.trace("Preparing files for " + analysisSubmission);
		PreparedWorkflowGalaxy preparedWorkflow = workspaceService.prepareAnalysisFiles(analysisSubmission);
		WorkflowInputsGalaxy input = preparedWorkflow.getWorkflowInputs();
		String libraryId = preparedWorkflow.getRemoteDataId();

		logger.trace("Executing " + analysisSubmission);
		galaxyWorkflowService.runWorkflow(input);

		AnalysisSubmission submittedAnalysis = analysisSubmissionService.update(analysisSubmission.getId(),
				ImmutableMap.of("analysisState", AnalysisState.RUNNING, "remoteInputDataId", libraryId));

		return new AsyncResult<>(submittedAnalysis);
	}

	/**
	 * Downloads and saves the results of an {@link AnalysisSubmission} that was
	 * previously submitted from an execution manager.
	 * 
	 * @param submittedAnalysis
	 *            An {@link AnalysisSubmission} that was previously submitted.
	 * @return A {@link Future} with an {@link AnalysisSubmission} object
	 *         containing information about the particular analysis.
	 * @throws ExecutionManagerException
	 *             If there was an issue with the execution manager.
	 * @throws IridaWorkflowNotFoundException
	 *             If the workflow for this submission could not be found in
	 *             IRIDA.
	 * @throws IOException
	 *             If there was an error loading the analysis results from an
	 *             execution manager.
	 * @throws IridaWorkflowAnalysisTypeException
	 *             If there was an issue building an {@link Analysis} object.
	 */
	@Transactional
	public Future<AnalysisSubmission> transferAnalysisResults(AnalysisSubmission submittedAnalysis)
			throws ExecutionManagerException, IOException, IridaWorkflowNotFoundException,
			IridaWorkflowAnalysisTypeException {
		checkNotNull(submittedAnalysis, "submittedAnalysis is null");
		checkNotNull(submittedAnalysis.getRemoteAnalysisId(), "remoteAnalysisId is null");
		if (!analysisSubmissionService.exists(submittedAnalysis.getId())) {
			throw new EntityNotFoundException("Could not find analysis submission for " + submittedAnalysis);
		}

		logger.debug("Getting results for " + submittedAnalysis);
		Analysis analysisResults = workspaceService.getAnalysisResults(submittedAnalysis);

		logger.trace("Saving results for " + submittedAnalysis);
		Analysis savedAnalysis = analysisService.create(analysisResults);

		AnalysisSubmission completedSubmission = analysisSubmissionService.update(submittedAnalysis.getId(),
				ImmutableMap.of("analysis", savedAnalysis, "analysisState", AnalysisState.COMPLETED));

		return new AsyncResult<>(completedSubmission);
	}
}
