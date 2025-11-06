import type { Driver, SaveDriver } from '@rendaxx/api-ts';
import { driversApi } from '@shared/api/client';
import { buildQueryParams } from '@shared/api/query';
import type { PageRequest, PageResult, RawPage } from '@shared/api/types';
import { toPageResult } from '@shared/api/types';

export async function fetchDrivers(request: PageRequest): Promise<PageResult<Driver>> {
  const params = buildQueryParams(request);
  const response = await driversApi.listDrivers({
    page: params.page,
    size: params.size,
    sort: params.sort,
    filter: params.filter
  });
  return toPageResult<Driver>(response as RawPage<Driver>);
}

export async function fetchDriver(id: number) {
  return driversApi.getDriver({ id });
}

export async function createDriver(payload: SaveDriver) {
  return driversApi.createDriver({ saveDriver: payload });
}

export async function updateDriver(id: number, payload: SaveDriver) {
  return driversApi.updateDriver({ id, saveDriver: payload });
}

export async function deleteDriver(id: number) {
  return driversApi.deleteDriver({ id });
}
