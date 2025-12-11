import type { Order, SaveOrder } from '@rendaxx/api-ts';
import { ordersApi } from '@shared/api/client';
import { buildQueryParams } from '@shared/api/query';
import type { PageRequest, PageResult, RawPage } from '@shared/api/types';
import { toPageResult } from '@shared/api/types';

export async function fetchOrders(request: PageRequest): Promise<PageResult<Order>> {
  const params = buildQueryParams(request);
  const response = await ordersApi.listOrders({
    page: params.page,
    size: params.size,
    sort: params.sort,
    filter: params.filter
  });
  return toPageResult<Order>(response as RawPage<Order>);
}

export async function fetchOrder(id: number) {
  return ordersApi.getOrder({ id });
}

export async function createOrder(payload: SaveOrder) {
  return ordersApi.createOrder({ saveOrder: payload });
}

export async function updateOrder(id: number, payload: SaveOrder) {
  return ordersApi.updateOrder({ id, saveOrder: payload });
}

export async function deleteOrder(id: number) {
  return ordersApi.deleteOrder({ id });
}
