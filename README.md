# Simulating the Fastest-Server-First routing policy 
Simulation of the Fastest-Server-First (FSF), also known as Closest-Facility-First and Closest Driver routing policy for 
the Multi-Class Multi-Server Queueing (MCMS) queueing systems:

## Applications of MCMS Queueing System
Minimizing queue waiting time in MCMS systems, where the service time depends both on the job type and the server type, has wide applications in transportation systems such as emergency networks and taxi networks, service systems such as call centers, and distributed computing platforms. However, the optimal dynamic policy for this problem is not characterized. In the absence of an optimal dynamic routing policy, FSF policy is widely used in practice and queueing literature.

## Framework: Multi-Class Multi-Server Queuing System
 In our multi-class multi-server queuing system, jobs (here contacts) of types $k \in K$ arrive according to independent Poisson processes with rates $d_k$.  The FSF router does not block any job. Also, there is no abandonment or retrial following the arrival stage. Each job must be routed to a server group $i \in F_k$, where $F_k$ is the set of server groups that are eligible to serve jobs of type $k$.  For completeness, we will denote $I$ as the full set of server groups (henceforth known simply as groups) and $F_i$ as the set of job types that can be served by group $i$. In the contact center literature, the terms job type and server group are referred to as call/contact type and agent group, respectively. At each server group $i$, there are $k_i$ identical servers. Service times are independent, each exponentially distributed with mean service time $\tau_{ki}$. 

## About the Fastest-Server-First policy
 When a job (or call in call centers) of type $i$ arrives, a job-to-group priority list for that job type determines the order in which the groups are checked for a free server (or agent in call centers). The priority list is an ordered list of all groups that can serve job $i$, i.e., $j \in F_i$, sorted from smallest to largest service times $\tau_{ij}$. The policy is called FSF because the fastest server has the highest priority in the list. There is one queue for each job type $i \in I$. If job $i$ finds all groups in $F_i$ busy, it will stay in queue $i$, where it will be served in first-come, first-served order. Similarly, when a server in group $j$ becomes free, a group-to-job list for that server group determines the order by which the server picks the next job to serve. The group-to-job list for group $j$ is a list of all job types that server group $j$ can serve, i.e., $i \in F_j$, sorted in increasing order by $\tau_{ij}$'s. If all queues in the list are empty, then the server stays free.

## Prerequisites (Libraries)
Before running this script, please install/import the following "ContactCenters" java simulation libraries in Java:
 - ContactCenters: http://simul.iro.umontreal.ca/contactcenters/index.html
- Stochastic Simulation in Java (SSJ): http://simul.iro.umontreal.ca/ssj/indexe.html

To learn how to import these Java packages, check their websites.

# Running for the first time
To run the program, go to 'mcms/main' and run the following Java code.
```
Main.java
```
By default, the program runs a test problem. You can feed your own instance as well.
## Test examples
There are test examples under the /resource/test folder. You can also simulate your own instances.
## Simulating your own models
To run a model, you would need to define the required data files:
* agents.dat
* demands.dat
* routes.dat

For a detailed instruction please read 'resoureces/test/input/GUIDE.txt' 
# Versioning
The current version is 2.0.

# Authors
Vahid Nourbakhsh: vahidnATuciDOTedu

# License
Apache license 2.0
Check the license files for the installed packages, as well.

# More
The following paper studies routing policies for MCMS systems:
Routing Heterogeneous Jobs to Heterogeneous Servers: A Global Optimization-Based Approach
https://ssrn.com/abstract=2967811
