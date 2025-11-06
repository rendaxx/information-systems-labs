import type { SaveVehicle, Vehicle } from '@rendaxx/api-ts';
import { vehiclesApi } from '@shared/api/client';
import { buildQueryParams } from '@shared/api/query';
import type { PageRequest, PageResult, RawPage } from '@shared/api/types';
import { toPageResult } from '@shared/api/types';

export async function fetchVehicles(request: PageRequest): Promise<PageResult<Vehicle>> {
  const params = buildQueryParams(request);
  const response = await vehiclesApi.listVehicles(params.page, params.size, params.sort, params.filter);
  return toPageResult<Vehicle>(response as RawPage<Vehicle>);
}

export async function fetchVehicle(id: number) {
  return vehiclesApi.getVehicle({ id });
}

export async function createVehicle(payload: SaveVehicle) {
  return vehiclesApi.createVehicle({ saveVehicle: payload });
}

export async function updateVehicle(id: number, payload: SaveVehicle) {
  return vehiclesApi.updateVehicle({ id, saveVehicle: payload });
}

export async function deleteVehicle(id: number) {
  return vehiclesApi.deleteVehicle({ id });
}
