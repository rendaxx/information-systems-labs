import type { RetailPoint, SaveRetailPoint } from '@rendaxx/api-ts';
import { retailPointsApi } from '@shared/api/client';
import { buildQueryParams } from '@shared/api/query';
import type { PageRequest, PageResult, RawPage } from '@shared/api/types';
import { toPageResult } from '@shared/api/types';

export async function fetchRetailPoints(request: PageRequest): Promise<PageResult<RetailPoint>> {
  const params = buildQueryParams(request);
  const response = await retailPointsApi.listRetailPoints({
    page: params.page,
    size: params.size,
    sort: params.sort,
    filter: params.filter
  });
  return toPageResult<RetailPoint>(response as RawPage<RetailPoint>);
}

export async function fetchRetailPoint(id: number) {
  return retailPointsApi.getRetailPoint({ id });
}

export async function createRetailPoint(payload: SaveRetailPoint) {
  return retailPointsApi.createRetailPoint({ saveRetailPoint: payload });
}

export async function updateRetailPoint(id: number, payload: SaveRetailPoint) {
  return retailPointsApi.updateRetailPoint({ id, saveRetailPoint: payload });
}

export async function deleteRetailPoint(id: number) {
  return retailPointsApi.deleteRetailPoint({ id });
}

export async function fetchNearestRetailPoints(id: number, limit: number) {
  return retailPointsApi.getNearestRetailPoints({ id, limit });
}
