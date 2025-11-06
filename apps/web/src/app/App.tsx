import { AppRouter } from './router';
import { AppProviders } from './providers/app-providers';

export function App() {
  return (
    <AppProviders>
      <AppRouter />
    </AppProviders>
  );
}
