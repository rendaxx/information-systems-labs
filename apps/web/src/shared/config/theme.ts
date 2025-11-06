export const THEME_STORAGE_KEY = 'isl:ui-theme';

export const THEMES = ['light', 'dark'] as const;

export type Theme = (typeof THEMES)[number];

export function isTheme(value: unknown): value is Theme {
  return typeof value === 'string' && (THEMES as readonly string[]).includes(value);
}
