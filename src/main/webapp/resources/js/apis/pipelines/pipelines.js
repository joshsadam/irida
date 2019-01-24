/**
 * Pipeline and workflow related API functions
 */
import axios from "axios";

const URL = `${window.TL.BASE_URL}pipelines`;

/**
 * Get the IRIDA workflow description info for a workflow
 * @param workflowUUID Workflow UUID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getIridaWorkflowDescription(workflowUUID) {
  try {
    const { data } = await axios({
      method: "get",
      url: `${URL}/ajax/${workflowUUID}`
    });
    return { data };
  } catch (error) {
    return { error: error };
  }
}

export const fetchIridaAnalysisWorkflows = async () =>
  axios.get(URL).then(response => response.data);
