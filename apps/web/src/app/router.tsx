import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom';
import { RootLayout } from './layouts/root-layout';
import { DriversPage } from '@pages/drivers/drivers-page';
import { OrdersPage } from '@pages/orders/orders-page';
import { RetailPointsPage } from '@pages/retail-points/retail-points-page';
import { RoutePointsPage } from '@pages/route-points/route-points-page';
import { RoutesPage } from '@pages/routes/routes-page';
import { SpecialsPage } from '@pages/specials/specials-page';
import { VehiclesPage } from '@pages/vehicles/vehicles-page';

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <Navigate to="/routes" replace /> },
      { path: 'routes', element: <RoutesPage /> },
      { path: 'orders', element: <OrdersPage /> },
      { path: 'drivers', element: <DriversPage /> },
      { path: 'vehicles', element: <VehiclesPage /> },
      { path: 'retail-points', element: <RetailPointsPage /> },
      { path: 'route-points', element: <RoutePointsPage /> },
      { path: 'specials', element: <SpecialsPage /> }
    ]
  }
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
