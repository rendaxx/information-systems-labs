import type { Route, SaveRoute } from '@rendaxx/api-ts';
import { routesApi } from '@shared/api/client';
import { buildQueryParams } from '@shared/api/query';
import type { PageRequest, PageResult, RawPage } from '@shared/api/types';
import { toPageResult } from '@shared/api/types';

export async function fetchRoutes(request: PageRequest): Promise<PageResult<Route>> {
  const params = buildQueryParams(request);
  const response = await routesApi.listRoutes(params.page, params.size, params.sort, params.filter);
  return toPageResult<Route>(response as RawPage<Route>);
}

export async function fetchRoute(id: number) {
  return routesApi.getRoute({ id });
}

export async function createRoute(payload: SaveRoute) {
  return routesApi.createRoute({ saveRoute: payload });
}

export async function updateRoute(id: number, payload: SaveRoute) {
  return routesApi.updateRoute({ id, saveRoute: payload });
}

export async function deleteRoute(id: number) {
  return routesApi.deleteRoute({ id });
}

export async function fetchAverageMileage() {
  return routesApi.getAverageRouteMileage();
}

export async function fetchRoutesWithinPeriod(periodStart: string, periodEnd: string) {
  return routesApi.getRoutesWithinPeriod({ periodStart, periodEnd });
}

export async function fetchRoutesByRetailPoint(retailPointId: number) {
  return routesApi.getRoutesByRetailPoint({ retailPointId });
}
