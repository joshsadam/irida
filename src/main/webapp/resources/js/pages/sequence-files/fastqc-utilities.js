/*
 * Constants file for the FastQC react components
 */

import { setBaseUrl } from "../../utilities/url-utilities";

export function getRootPath(pathname) {
  const BASE = setBaseUrl(""); // will either be a '/' or '/[contextPath]/'
  const path = pathname.match(new RegExp(`${BASE}([a-zA-Z]*)`))[1];
  const possible = pathname.match(/(charts|overrepresented|details)$/);
  const route = possible ? possible[0] : "charts";

  switch (path) {
    case "projects":
      return [
        `${BASE}projects/:projectId/samples/:sampleId/sequenceFiles/:sequenceObjectId/file/:fileId/`,
        route,
      ];
    case "samples":
      return [
        `${BASE}samples/:sampleId/sequenceFiles/:sequenceObjectId/file/:fileId/`,
        route,
      ];
    case "sequenceFiles":
      return [`${BASE}sequenceFiles/:sequenceObjectId/file/:fileId/`, route];
    case "sequencingRuns":
      return [
        `${BASE}sequencingRuns/:runId/sequenceFiles/:seqObjId/file/:fileId/`,
        route,
      ];
    default:
      throw new Error("Cannot find path for sequencing file");
  }
}
