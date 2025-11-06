export const queryKeys = {
  routes: {
    all: ['routes'] as const,
    list: (params: unknown) => ['routes', 'list', params] as const,
    detail: (id: number) => ['routes', 'detail', id] as const,
    metrics: ['routes', 'metrics'] as const
  },
  orders: {
    all: ['orders'] as const,
    list: (params: unknown) => ['orders', 'list', params] as const,
    detail: (id: number) => ['orders', 'detail', id] as const
  },
  drivers: {
    all: ['drivers'] as const,
    list: (params: unknown) => ['drivers', 'list', params] as const,
    detail: (id: number) => ['drivers', 'detail', id] as const
  },
  vehicles: {
    all: ['vehicles'] as const,
    list: (params: unknown) => ['vehicles', 'list', params] as const,
    detail: (id: number) => ['vehicles', 'detail', id] as const
  },
  retailPoints: {
    all: ['retailPoints'] as const,
    list: (params: unknown) => ['retailPoints', 'list', params] as const,
    detail: (id: number) => ['retailPoints', 'detail', id] as const,
    nearest: (id: number, limit: number) => ['retailPoints', 'nearest', id, limit] as const
  },
  routePoints: {
    all: ['routePoints'] as const,
    list: (params: unknown) => ['routePoints', 'list', params] as const,
    detail: (id: number) => ['routePoints', 'detail', id] as const,
    topRetailPoints: (limit: number) => ['routePoints', 'topRetailPoints', limit] as const
  }
};
