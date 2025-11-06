import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './app/App';
import './app/styles/index.css';

const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('Не удалось найти корневой элемент #root');
}

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
