export interface PageRequest {
  page: number;
  size: number;
  sort?: SortOption[];
  filter?: Record<string, string | number | undefined>;
}

export interface SortOption {
  field: string;
  order: 'asc' | 'desc';
}

export interface PageMetadata {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sort: string[];
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface PageResult<T> {
  data: T[];
  meta: PageMetadata;
}

export interface RawPage<T> extends PageMetadata {
  content?: T[];
}

export function toPageResult<T>(page: RawPage<T>): PageResult<T> {
  const { content, ...meta } = page;
  return {
    data: content ?? [],
    meta
  };
}
