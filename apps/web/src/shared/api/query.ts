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
      // Encode deepObject as bracketed keys to match server expectations: filter[foo]=bar
      const encoded: Record<string, string> = {};
      for (const [key, value] of entries) {
        encoded[`filter[${key}]`] = String(value);
      }
      query.filter = encoded;
    }
  }

  return query;
}

function formatSortOption(option: SortOption): string {
  return `${option.field},${option.order}`;
}
