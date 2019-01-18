import { call, put, take } from "redux-saga/effects";
import { cartPageTypes, cartPageActions } from "./reducer";
import {
  types as globalCartTypes,
  actions as globalCartActions
} from "../../redux/reducers/cart";
import {
  getCartIds,
  emptyCart,
  getSamplesForProject
} from "../../apis/cart/cart";

export function* getCartProjectIds() {
  const { count } = yield take(globalCartTypes.INITIALIZED);
  if (count > 0) {
    const { ids } = yield call(getCartIds);
    for (let id of ids) {
      const samples = yield call(getSamplesForProject, id);
      yield put(cartPageActions.samplesLoaded(samples));
    }
  }
}

export function* empty() {
  yield take(cartPageTypes.CART_EMPTY);
  yield call(emptyCart);
  yield put(globalCartActions.updated({ count: 0 }));
}
