# Kubernetes Kit Rolling Updates Demo with ArgoCD + Argo Rollouts

## Prerequisites

- Docker Desktop with Kubernetes enabled
- `kubectl`, `argocd`, `kubectl-argo-rollouts` CLIs installed

---

Uses Docker Desktop Kubernetes with ArgoCD + Argo Rollouts.

### Install ArgoCD

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Fix missing ApplicationSet CRD (annotation too large for default apply)
kubectl apply -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/crds/applicationset-crd.yaml --server-side

# Restart the applicationset controller after CRD is applied
kubectl rollout restart deployment argocd-applicationset-controller -n argocd
```

### Install nginx ingress

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.12.1/deploy/static/provider/cloud/deploy.yaml
```

> **Note:** As of Ingress NGINX v1.9.0 (helm chart 4.8.0), `configuration-snippet` annotations are disabled by default for security. Enable them once after install:

```bash
kubectl -n ingress-nginx patch configmap ingress-nginx-controller \
  --type merge \
  -p '{"data":{"allow-snippet-annotations":"true","annotations-risk-level":"Critical"}}'

kubectl rollout restart deployment ingress-nginx-controller -n ingress-nginx
kubectl rollout status deployment ingress-nginx-controller -n ingress-nginx
```

### Install Argo Rollouts

```bash
kubectl create namespace argo-rollouts
kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
brew install argoproj/tap/kubectl-argo-rollouts
```

### Install CloudNativePG operator

```bash
kubectl apply --server-side -f \
  https://raw.githubusercontent.com/cloudnative-pg/cloudnative-pg/release-1.25/releases/cnpg-1.25.1.yaml

# Wait for the operator to be ready
kubectl rollout status deployment cnpg-controller-manager -n cnpg-system
```

### Access ArgoCD UI

```bash
# In a separate terminal — keep this running
kubectl port-forward svc/argocd-server -n argocd 8090:443

# Get initial admin password
kubectl get secret argocd-initial-admin-secret -n argocd \
  -o jsonpath="{.data.password}" | base64 -d

# Log in
argocd login localhost:8090 --username admin --password <password> --insecure
```

Open `https://localhost:8090` in the browser.

### Access the app

```bash
# In a separate terminal — keep this running
kubectl port-forward svc/ingress-nginx-controller -n ingress-nginx 8080:80
```

Open `http://localhost:8080` in the browser.

### Create the Vaadin subscription key secret

This secret is not in the repo (public). Create it manually in the cluster once before ArgoCD syncs:

```bash
kubectl create secret generic vaadin-subscription-key \
  --from-literal=subscriptionKey=<your-pro-key>
```

### Create the ArgoCD Application

```bash
argocd app create kits-demo \
  --repo https://github.com/onuridrisoglu/kk-demo.git \
  --path k8s \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace default \
  --sync-policy automated \
  --self-heal \
  --directory-recurse
```

### Build and push Docker images

```bash
# Build for linux/amd64 (required on Apple Silicon)
docker build --secret id=proKey,src=$HOME/.vaadin/proKey --platform linux/amd64 -t onurvaadin/kits-demo:1.0 .

# Tag for v2 (or rebuild with updated code)
docker tag onurvaadin/kits-demo:1.0 onurvaadin/kits-demo:2.0

# Push both
docker push onurvaadin/kits-demo:1.0
docker push onurvaadin/kits-demo:2.0
```

### Rollout flow

**Release v1 (initial deploy)**
- Build and push `onurvaadin/kits-demo:1.0`
- Push `k8s/` to `kk-demo` → ArgoCD syncs → v1 pods start

**Release v2 (canary rollout)**

1. Bump image to `2.0` and `APP_VERSION` to `2.0` in `k8s/apps/rollout.yaml`, push → Argo Rollouts deploys v2 canary but no traffic yet (`setWeight: 0`):
   ```bash
   git add k8s/apps/rollout.yaml
   git commit -m "Release v2 canary"
   git push
   ```
2. Verify v2 via the canary service:
   ```bash
   kubectl port-forward svc/kits-demo-canary 8081:80
   # open http://localhost:8081
   ```
3. Promote — shifts all **new** sessions to v2 (`setWeight: 100`), existing v1 sticky sessions remain on v1:
   ```bash
   kubectl argo rollouts promote kits-demo
   ```
4. Notify active v1 users (kubectl only, no file edits):
   ```bash
   kubectl annotate ingress kits-demo-stable \
     'nginx.ingress.kubernetes.io/configuration-snippet=proxy_set_header X-AppUpdate "2.0";' \
     --overwrite
   ```
   nginx injects `X-AppUpdate: 2.0` into requests reaching the v1 pods → Vaadin's `RollingUpdateHandler` detects the version mismatch and pushes the notification popup.
5. v1 users see "new version available" popup, click "click here" → sticky cookie cleared → next request routes them to v2 (now at 100%)
6. Once v1 is drained, promote and clean up:
   ```bash
   kubectl argo rollouts promote kits-demo

   kubectl annotate ingress kits-demo-stable \
     nginx.ingress.kubernetes.io/configuration-snippet- \
     --overwrite
   ```
   v2 becomes stable, v1 pods scale down.

### Monitor rollout state

```bash
# Watch live rollout progress (steps, canary weight, pod status)
kubectl argo rollouts get rollout kits-demo --watch
```

### Promote commands

```bash
# Advance to the next step (use at steps 3 and 6)
kubectl argo rollouts promote kits-demo

# Skip all remaining steps and complete the rollout immediately
kubectl argo rollouts promote kits-demo --full
```
