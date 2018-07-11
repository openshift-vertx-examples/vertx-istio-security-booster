# Eclipse Vert.x / Istio Security Booster

## Purpose
Showcase Istio TLS and ACL via a set of Eclipse Vert.x applications.

## Prerequisites
* Openshift 3.9 cluster
* Istio
* Create a new project/namespace on the cluster. This is where your application will be deployed.

```bash
oc login -u system:admin
oc adm policy add-cluster-role-to-user admin developer --as=system:admin
oc login -u developer -p developer
oc new-project <whatever valid project name you want> # not required, keep a trace of this name.
```

## Build and deploy the application

### With Fabric8 Maven Plugin (FMP)
Execute the following command to build the project and deploy it to OpenShift:

```bash
mvn clean fabric8:deploy -Popenshift
```

Configuration for FMP may be found both in pom.xml and `src/main/fabric8` files/folders.

This configuration is used to define service names and deployments that control how pods are labeled/versioned on the OpenShift cluster.

### With S2I Build

find . | grep openshiftio | grep application | xargs -n 1 oc apply -f

oc new-app --template=vertx-istio-security-name -p SOURCE_REPOSITORY_URL=https://github.com/openshiftio-vertx-boosters/vertx-istio-security-booster -p SOURCE_REPOSITORY_REF=master -p SOURCE_REPOSITORY_DIR=vertx-istio-security-name
oc new-app --template=vertx-istio-security-greeting -p SOURCE_REPOSITORY_URL=https://github.com/openshiftio-vertx-boosters/vertx-istio-security-booster -p SOURCE_REPOSITORY_REF=master -p SOURCE_REPOSITORY_DIR=vertx-istio-security-greeting

## Use Cases

Configure the ingress gateway with:

```bash
oc apply -f rules/gateway.yaml
```

### Scenario #1. Mutual TLS

This scenario demonstrates a mutual transport level security between the services.

1. Open the booster’s web page via Istio ingress route
    ```bash
    echo http://$(oc get route istio-ingress -o jsonpath='{.spec.host}{"\n"}' -n istio-system)/
    ```
2. "Hello, World!" should be returned after invoking `greeting` service.
3. Now modify greeting deployment to disable sidecar injection by replacing the 2 occurrences of `sidecar.istio.io/inject` values to `false`
    ```bash
    oc edit deploymentconfigs/vertx-istio-security-greeting
    ```
4. Got back to the booster page, and without reloading, try to invoke the service. The invocation failed. The `Greeting`
 service invocation fails with a reset connection, because the `greeting` service has to be inside a service mesh in 
 order to access the `name` service.
5. Cleanup by setting `sidecar.istio.io/inject` values to true (the 2 occurrences)
    ```bash
    oc edit deploymentconfigs/vertx-istio-security-greeting
    ```

### Scenario #2. Access control

This scenario demonstrates access control when using mutual TLS. In order to access a name service, calling service has to have a specific label and service account name.

1. Open the booster’s web page via Istio ingress route
    ```bash
    echo http://$(oc get route istio-ingress -o jsonpath='{.spec.host}{"\n"}' -n istio-system)/
    ```
2. "Hello, World!" should be returned after invoking `greeting` service.
3. Configure Istio Mixer to block `greeting` service from accessing `name` service
    ```bash
    oc apply -f rules/block-greeting-service.yaml
    ```
4. `Greeting` service invocations to the `name` service will be forbidden.
5. Configure Istio Mixer to only allow requests from `greeting` service and with `sa-greeting` service account to access 
`name` service
    ```bash
    oc apply -f <(sed -e "s/TARGET_NAMESPACE/$(oc project -q)/g" rules/require-service-account-and-label.yaml)
    ```
6. "Hello, World!" should be returned after invoking `greeting` service.
7. Cleanup
    ```bash
    oc delete -f rules/require-service-account-and-label.yaml
    ```

## Undeploy the application

### With Fabric8 Maven Plugin (FMP)

```bash
mvn fabric8:undeploy
```

### Remove the namespace
This will delete the project from the OpenShift cluster

```bash
oc delete project <your project name>
```
