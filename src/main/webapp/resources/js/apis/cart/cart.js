import axios from "axios";

const url = `${window.TL.BASE_URL}cart`;

/**
 * Add samples for a project to the cart.
 * @param {number} projectId Identifier for the project the samples are from.
 * @param {array} samples array of sample {ids } to add to cart.
 * @returns {AxiosPromise<any>}
 */
export const putSampleInCart = async (projectId, samples) =>
  axios.put(url, {
    projectId,
    samples
  });

/**
 * Get the current number of samples in the cart
 * @returns {Promise<void>}
 */
export const getCartCount = async () => {
  return axios.get(`${url}/count`).then(response => ({ count: response.data }));
};

/**
 * Get the current state of the cart.
 * @returns {Promise<void | never>}
 */
export const getCart = async () =>
  axios.get(`${url}`).then(response => response.data);

export const getProjectsInCart = async () =>
  axios.get(`${url}/projects`).then(response => response.data);
