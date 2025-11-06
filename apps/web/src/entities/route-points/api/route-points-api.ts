import type { RoutePoint, SaveRoutePoint, RetailPoint } from '@rendaxx/api-ts';
import { routePointsApi } from '@shared/api/client';
import { buildQueryParams } from '@shared/api/query';
import type { PageRequest, PageResult, RawPage } from '@shared/api/types';
import { toPageResult } from '@shared/api/types';

export async function fetchRoutePoints(request: PageRequest): Promise<PageResult<RoutePoint>> {
  const params = buildQueryParams(request);
  const response = await routePointsApi.listRoutePoints({
    page: params.page,
    size: params.size,
    sort: params.sort,
    filter: params.filter
  });
  return toPageResult<RoutePoint>(response as RawPage<RoutePoint>);
}

export async function fetchRoutePoint(id: number) {
  return routePointsApi.getRoutePoint({ id });
}

export async function createRoutePoint(payload: SaveRoutePoint) {
  return routePointsApi.createRoutePoint({ saveRoutePoint: payload });
}

export async function updateRoutePoint(id: number, payload: SaveRoutePoint) {
  return routePointsApi.updateRoutePoint({ id, saveRoutePoint: payload });
}

export async function deleteRoutePoint(id: number) {
  return routePointsApi.deleteRoutePoint({ id });
}

export async function fetchTopRetailPoints(limit: number): Promise<RetailPoint[]> {
  return routePointsApi.getTopRetailPoints(limit);
}
