import {
  Configuration,
  DriversApi,
  OrdersApi,
  RetailPointsApi,
  RoutePointsApi,
  RoutesApi,
  VehiclesApi
} from '@rendaxx/api-ts';

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

const configuration = new Configuration({ basePath: baseUrl });

export const routesApi = new RoutesApi(configuration);
export const ordersApi = new OrdersApi(configuration);
export const driversApi = new DriversApi(configuration);
export const vehiclesApi = new VehiclesApi(configuration);
export const retailPointsApi = new RetailPointsApi(configuration);
export const routePointsApi = new RoutePointsApi(configuration);
