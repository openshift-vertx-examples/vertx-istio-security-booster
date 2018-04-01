_Eclipse Vert.x Booster illustrating the usage of TLS communication managed by Istio._

# Install Istio

Create Istio service accounts
```
oc adm policy add-scc-to-user anyuid -z istio-ingress-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-grafana-service-account -n istio-system
oc adm policy add-scc-to-user anyuid -z istio-prometheus-service-account -n istio-system
```

Download Istio
```
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=0.5.0 sh -
```

Install Istio
```
cd istio-0.5.0
export PATH=$PWD/bin:$PATH
oc login -u system:admin
kubectl apply -f install/kubernetes/istio-auth.yaml
```

Create Ingress route
```
oc expose svc istio-ingress -n istio-system
oc get route/istio-ingress -n istio-system
```

# Prepare the Namespace

**This mission assumes that `myproject` namespace is used.**

Create the namespace if one does not exist
```
oc new-project myproject
```

Give required permissions to the service accounts used by the booster
```
oc adm policy add-scc-to-user privileged -n myproject -z default
oc adm policy add-scc-to-user privileged -n myproject -z sa-greeting
```

# Deploy the Application

```
mvn clean package fabric8:build -Popenshift
oc apply -f <(istioctl kube-inject -f kubernetes/application.yaml)
```

# Use the Application

Get ingress route (further referred as ${INGRESS_ROUTE})
```
oc get route -n istio-system
```

Copy and paste HOST/PORT value returned by the previous command to your browser.

To only allow the greeting service access to the name service, run:

```
istioctl create -f kubernetes/istio-rule-require-service-account.yaml -n myproject
```

To allow everyone to access the name service:
```
istioctl delete -f kubernetes/istio-rule-require-service-account.yaml -n myproject
```
