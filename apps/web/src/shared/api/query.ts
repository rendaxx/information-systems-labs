import type { PageRequest, SortOption } from './types';

interface QueryParams {
  page?: number;
  size?: number;
  sort?: string[];
  filter?: Record<string, string>;
}

export function buildQueryParams(params: PageRequest): QueryParams {
  const query: QueryParams = {
    page: params.page,
    size: params.size
  };

  if (params.sort && params.sort.length > 0) {
    query.sort = params.sort.map(formatSortOption);
  }

  if (params.filter) {
    const entries = Object.entries(params.filter).filter(([, value]) => value !== undefined && value !== '');
    if (entries.length > 0) {
      query.filter = Object.fromEntries(entries.map(([key, value]) => [key, String(value)]));
    }
  }

  return query;
}

function formatSortOption(option: SortOption): string {
  return `${option.field},${option.order}`;
}
