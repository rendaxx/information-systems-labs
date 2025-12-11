import { useThemeContext } from '@app/providers/theme-provider';
import { Button } from '@shared/ui/button';
import { MoonStar, Sun } from 'lucide-react';

export function ThemeToggle() {
  const { theme, toggleTheme } = useThemeContext();
  const Icon = theme === 'light' ? MoonStar : Sun;

  return (
    <Button
      variant="ghost"
      size="icon"
      type="button"
      onClick={toggleTheme}
      aria-label="Переключить тему"
      className="rounded-full"
    >
      <Icon className="h-4 w-4" />
    </Button>
  );
}
