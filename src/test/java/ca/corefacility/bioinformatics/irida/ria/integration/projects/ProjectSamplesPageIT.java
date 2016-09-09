package ca.corefacility.bioinformatics.irida.ria.integration.projects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectSamplesPage;

import com.github.springtestdbunit.annotation.DatabaseSetup;

/**
 * <p>
 * Integration test to ensure that the Project Details Page.
 * </p>
 *
 */
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/projects/ProjectSamplesView.xml")
public class ProjectSamplesPageIT extends AbstractIridaUIITChromeDriver {
	private static final Logger logger = LoggerFactory.getLogger(ProjectSamplesPageIT.class);

	@Before
	public void init() {
		LoginPage.loginAsManager(driver());
	}

	@Test(expected = AssertionError.class)
	public void testGoingToInvalidPage() {
		logger.debug("Testing going to an invalid sample id");
		ProjectSamplesPage.gotToPage(driver(), 100);
	}

	@Test
	public void testPageSetUp() {
		logger.info("Testing page set up for: Project Samples");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		assertTrue("Should have the project name as the page main header.", page.getTitle().equals("project ID 1"));
		assertEquals("Should display 10 projects initially.", 10, page.getNumberProjectsDisplayed());
	}

	@Test
	public void testToolbarButtons() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		// Test set up with no sample selected
		page.openToolsDropDown();
		assertFalse("Merge option should not be enabled", page.isMergeBtnEnabled());
		assertFalse("Copy option should not be enabled", page.isCopyBtnEnabled());
		assertFalse("Move option should not be enabled", page.isMoveBtnEnabled());
		assertFalse("Remove option should not be enabled", page.isRemoveBtnEnabled());
		page.openExportDropdown();
		assertFalse("Download option should not be enabled", page.isDownloadBtnEnabled());
		assertFalse("NCBI Export option should not be enabled", page.isNcbiBtnEnabled());

		// Test with one sample selected
		page.selectSample(0);
		page.openToolsDropDown();
		assertFalse("Merge option should not be enabled", page.isMergeBtnEnabled());
		assertTrue("Copy option should be enabled", page.isCopyBtnEnabled());
		assertTrue("Move option should be enabled", page.isMoveBtnEnabled());
		assertTrue("Remove option should be enabled", page.isRemoveBtnEnabled());
		page.openExportDropdown();
		assertTrue("Download option should be enabled", page.isDownloadBtnEnabled());
		assertTrue("NCBI Export option should be enabled", page.isNcbiBtnEnabled());

		// Test with two samples selected
		page.selectSample(1);
		page.openToolsDropDown();
		assertTrue("Merge option should be enabled", page.isMergeBtnEnabled());
		assertTrue("Copy option should be enabled", page.isCopyBtnEnabled());
		assertTrue("Move option should be enabled", page.isMoveBtnEnabled());
		assertTrue("Remove option should be enabled", page.isRemoveBtnEnabled());
		page.openExportDropdown();
		assertTrue("Download option should be enabled", page.isDownloadBtnEnabled());
		assertTrue("NCBI Export option should be enabled", page.isNcbiBtnEnabled());

	}

	@Test
	public void testPaging() {
		logger.info("Testing paging for: Project Samples");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		assertFalse("'Previous' button should be disabled", page.isPreviousBtnEnabled());
		assertTrue("'Next' button should be enabled", page.isNextBtnEnabled());
		assertEquals("Should be 3 pages of samples", 3, page.getPaginationCount());
	}

	@Test
	public void testAssociatedProjects() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		assertEquals("Should be displaying 21 samples", "Showing 1 to 10 of 21 entries", page.getTableInfo());
		page.displayAssociatedProject();
		assertEquals("Should be displaying 22 samples", "Showing 1 to 10 of 22 entries", page.getTableInfo());
	}

	@Test
	public void testSampleSelection() {
		logger.info("Testing sample selection for: Project Samples");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		assertEquals("Should be 0 selected samples", "No samples selected", page.getSelectedInfoText());

		page.selectSample(0);
		assertEquals("Should be 1 selected samples", "1 sample selected", page.getSelectedInfoText());

		page.selectSampleWithShift(4);
		assertEquals("Should be 5 selected samples", "5 samples selected", page.getSelectedInfoText());

		page.selectAllSamples();
		assertEquals("Should have all samples selected", "21 samples selected", page.getSelectedInfoText());

		page.deselectAllSamples();
		assertEquals("Should be 0 selected samples", "No samples selected", page.getSelectedInfoText());

		page.selectPage();
		assertEquals("Should be 10 selected samples", "10 samples selected", page.getSelectedInfoText());

		page.selectAllSamples();
		assertEquals("Should have all samples selected", "21 samples selected", page.getSelectedInfoText());

		page.deselectPage();
		assertEquals("Should have all samples selected", "11 samples selected", page.getSelectedInfoText());

	}

	@Test
	public void testAddSamplesToCart() {
		logger.info("Testing adding samples to the global cart.");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		page.selectSample(0);
		page.selectSampleWithShift(4);
		assertEquals("Should be 5 selected samples", "5 samples selected", page.getSelectedInfoText());

		page.addSelectedSamplesToCart();
		assertEquals("Should be 5 samples in the cart", 5, page.getCartCount());

	}

	@Test
	public void testMergeSamples() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		// Select some samples
		page.selectSample(0);
		page.selectSample(1);
		assertEquals("Should be 2 selected samples", "2 samples selected", page.getSelectedInfoText());

		// Merge these samples with the original name
		List<String> originalNames = page.getSampleNamesOnPage().subList(0, 2); // Only need the first two
		page.mergeSamplesWithOriginalName();
		List<String> mergeNames = page.getSampleNamesOnPage().subList(0, 2);
		assertEquals("Should still the first samples name", originalNames.get(0), mergeNames.get(0));
		assertFalse("Should have different sample second since it was merged", originalNames.get(1).equals(mergeNames.get(1)));

		// Merge with a new name
		page.selectSample(0);
		page.selectSample(1);
		String newSampleName = "MY_NEW_SAMPLE_NAME";
		page.mergeSamplesWithNewName(newSampleName);
		String name = page.getSampleNamesOnPage().get(0);
		assertEquals("Should have the new sample name", newSampleName, name);
	}

	@Test
	public void testCopySamples() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		page.selectSample(0);
		page.selectSample(1);

		List<String> names = page.getSampleNamesOnPage().subList(0, 1);
		String newProjectName = "project4";
		page.copySamples(newProjectName);

		ProjectSamplesPage newPage = ProjectSamplesPage.gotToPage(driver(), 4);
		List<String> newNames = newPage.getSampleNamesOnPage().subList(0, 1);

		for(int i = 0; i == names.size(); i++) {
			assertEquals("Should have the same samples since they were copied", names.get(i), newNames.get(i));
		}
	}

	@Test
	public void testMoveSamples() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		assertEquals("Should be displaying 21 samples", "Showing 1 to 10 of 21 entries", page.getTableInfo());
		List<String> movedNames = page.getSampleNamesOnPage().subList(2, 3);
		page.selectSample(2);
		page.selectSample(3);
		page.moveSamples("project3");
		assertEquals("Should be displaying 19 samples", "Showing 1 to 10 of 19 entries", page.getTableInfo());


		ProjectSamplesPage.gotToPage(driver(), 3);
		List<String> newNames = page.getSampleNamesOnPage().subList(0, 1);

		for(int i = 0; i == movedNames.size(); i++) {
			assertEquals("Should have the same samples since they were copied", movedNames.get(i), newNames.get(i));
		}
	}

	@Test
	public void testRemoveSamplesFromProject() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		// Select some samples
		page.selectSample(0);
		page.selectSample(1);

		// Remove process
		page.removeSamples();
		assertEquals("Should be only 2 pages of projects now", 2, page.getPaginationCount());
		page.selectPaginationPage(2);
		assertEquals("Should only be displaying 9 samples.", 9, page.getNumberProjectsDisplayed());
		assertEquals("Should be 0 selected samples", "No samples selected", page.getSelectedInfoText());
	}

	@Test
	public void testFilteringSamplesByProperties() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		assertEquals("Should have 21 projects displayed", "Showing 1 to 10 of 21 entries", page.getTableInfo());
		page.filterByName("5");
		assertEquals("Should have 17 projects displayed", "Showing 1 to 10 of 17 entries", page.getTableInfo());
		page.filterByName("52");
		assertEquals("Should have 17 projects displayed", "Showing 1 to 3 of 3 entries", page.getTableInfo());

		// Test clearing the filters
		page.clearFilter();
		assertEquals("Should have 21 projects displayed", "Showing 1 to 10 of 21 entries", page.getTableInfo());

		// Should ignore case
		page.filterByName("sample");
		assertEquals("Should ignore case when filtering", "Showing 1 to 10 of 21 entries", page.getTableInfo());

		// Test date range filter
		page.clearFilter();
		assertEquals("Should have 21 projects displayed", "Showing 1 to 10 of 21 entries", page.getTableInfo());
	}

	@Test
	public void testFilteringWithDates() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		page.filterByDateRange("07/06/2015", "07/09/2015");
		assertEquals("Should ignore case when filtering", "Showing 1 to 4 of 4 entries", page.getTableInfo());

		// Test clearing the filters
		page.clearFilter();
		assertEquals("Should have 21 samples displayed", "Showing 1 to 10 of 21 entries", page.getTableInfo());
	}

	@Test
	public void testCartFunctionality() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		// Select some samples
		page.selectSample(0);
		page.selectSample(1);

		// Add them to the cart
		page.addSelectedSamplesToCart();
		assertEquals("Should be two items in the cart", 2, page.getCartCount());

		page.selectSample(5);
		page.addSelectedSamplesToCart();
		assertEquals("Should be three items in the cart", 3, page.getCartCount());
	}

	@Test
	public void testLinkerFunctionality() {
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		assertEquals("Should display the correct linker for entire project", "ngsArchive.pl -p 1",
				page.getLinkerText());

		// Select some samples
		page.selectSample(0);
		page.selectSample(1);

		// Open the linker modal
		assertEquals("Should display the correct linker command", "ngsArchive.pl -p 1 -s 1 -s 2", page.getLinkerText());
	}
}
