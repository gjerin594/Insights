#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
#! /bin/sh
# /etc/init.d/InSightsBitBucketCloudAgent

### BEGIN INIT INFO
# Provides: Runs a Python script on startup
# Required-Start: BootPython start
# Required-Stop: BootPython stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run python program at boot
# Description: Runs a python program at boot
### END INIT INFO
#export INSIGHTS_AGENT_HOME=/home/ec2-user/insightsagents
source /etc/profile

case "$1" in
  start)
    if [[ $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketCloudAgent already running"
    else
     echo "Starting InSightsBitBucketCloudAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/bitbucketcloud
     python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucketcloud.BitBucketCloudAgent import BitBucketCloudAgent; BitBucketCloudAgent()" &
    fi
    if [[ $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketCloudAgent Started Sucessfully"
    else
     echo "InSightsBitBucketCloudAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsBitBucketCloudAgent"
    if [[ $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}')
    else
     echo "InSIghtsBitBucketCloudAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketCloudAgent Failed to Stop"
    else
     echo "InSightsBitBucketCloudAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsBitBucketCloudAgent"
    if [[ $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketCloudAgent stopping"
     sudo kill -9 $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}')
     echo "InSightsBitBucketCloudAgent stopped"
     echo "InSightsBitBucketCloudAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/bitbucketcloud
     python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucketcloud.BitBucketCloudAgent import BitBucketCloudAgent; BitBucketCloudAgent()" &
     echo "InSightsBitBucketCloudAgent started"
    else
     echo "InSightsBitBucketCloudAgent already in stopped state"
     echo "InSightsBitBucketCloudAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/bitbucketcloud
     python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucketcloud.BitBucketCloudAgent import BitBucketCloudAgent; BitBucketCloudAgent()" &
     echo "InSightsBitBucketCloudAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsBitBucketCloudAgent"
    if [[ $(ps aux | grep '[s]cm.bitbucketcloud.BitBucketCloudAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketCloudAgent is running"
    else
     echo "InSightsBitBucketCloudAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsBitBucketCloudAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
