# BlazeMeter Java SDK

We, at BlazeMeter, aim to bring you tools for quicker, and easier development.

As part of this ambition, we are proud to present our Java SDK!

Read more about this library on our [wiki page](wiki/).

[![Build Status](https://travis-ci.org/Blazemeter/blazemeter-api-client.svg?branch=master)](https://travis-ci.org/Blazemeter/blazemeter-api-client)
[![codecov](https://codecov.io/gh/Blazemeter/blazemeter-api-client/branch/master/graph/badge.svg)](https://codecov.io/gh/Blazemeter/blazemeter-api-client)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e86b726f20e046a2b89fc13c86ca6f87)](https://www.codacy.com/app/dzmitrykashlach/blazemeter-api-client?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Blazemeter/blazemeter-api-client&amp;utm_campaign=Badge_Grade)

Supported calls:  

|Description   |Method   |Path   |
|:---|:---|:---|
|get master status code |GET   |/api/v4/masters/{id}/status?events=false   |
|get tests|GET   |/api/v4/tests?limit=10000&workspaceId=<workspaceId>   |   
|start test   |POST   |/api/v4/tests/{id}/start   |   
|start external test   |POST   |/api/v4/tests/{id}/start-external   |   
|start collection   |POST   |/api/v4/collections/{id}/start   |   
|stop master   |POST   |/api/v4/masters/{id}/stop   |   
|terminate master   |POST   |/api/v4/masters/{id}/terminate   |   
|test report   |GET   |/api/v4/masters/{id}/reports/main/summary   |   
|get user   |GET   |/api/v4/user   |   
|get ci status   |GET   |/api/v4/masters/{id}/ci-status   |   
|retrieve junit report   |GET   |/api/v4/masters/{id}/reports/thresholds?format=junit   |   
|retrieve jtl report   |GET   |/api/v4/sessions/{id}/reports/logs   |   
|generate public token   |POST   |/api/v4/masters/{id}/public-token   |   
|master info   |GET,PATCH   |/api/v4/masters/{id}   |   
|properties   |POST   |/api/v4/sessions/{id}/properties?target=all   |   
|collection config   |GET   |/api/v4/collections/{id}   |   
|test config   |GET   |/api/v4/tests/{id}   |   
|list of test sessions   |GET   |/api/v4/masters/{id}/sessions   |   
|workspaces   |GET   |/api/v4/workspaces?limit=1000&enabled=true   |   
|accounts   |GET   |/api/v4/accounts   |   
|create workspace   |POST   |/api/v4/workspaces   |   
|start external test   |POST   |/api/v4/sessions   |   
|create test   |POST   |/api/v4/tests   |   
|get projects   | GET   |/api/v4/projects?workspaceId={id}&limit=99999   |   

	
