import {
  Layers as RoutesIcon,
  ClipboardList as OrdersIcon,
  Users as DriversIcon,
  Truck as VehiclesIcon,
  Building2 as RetailPointsIcon,
  Route as RoutePointsIcon,
  Activity as SpecialsIcon
} from 'lucide-react';
import type { ComponentType } from 'react';
import type { IconProps } from 'lucide-react';

export interface NavItem {
  label: string;
  path: string;
  icon: ComponentType<IconProps>;
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Маршруты', path: '/routes', icon: RoutesIcon },
  { label: 'Заказы', path: '/orders', icon: OrdersIcon },
  { label: 'Водители', path: '/drivers', icon: DriversIcon },
  { label: 'Транспорт', path: '/vehicles', icon: VehiclesIcon },
  { label: 'Торговые точки', path: '/retail-points', icon: RetailPointsIcon },
  { label: 'Точки маршрута', path: '/route-points', icon: RoutePointsIcon },
  { label: 'Спецоперации', path: '/specials', icon: SpecialsIcon }
];
