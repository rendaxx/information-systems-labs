FROM nginx:1.27-alpine

# Copy static build artifacts produced by CI into the nginx html dir
COPY apps/web/dist/ /usr/share/nginx/html/

# Nginx config with SPA fallback and API/WebSocket proxy to backend `app:8080`
COPY apps/web/deploy/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

