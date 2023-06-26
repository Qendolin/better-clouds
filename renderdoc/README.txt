Just create a file called 'renderdoc.bat' or 'renderdoc.sh' and copy the first line from the intellij idea console
when you run the game. Then remove the 'agentlib' argument and add '-Dfabric.development=true'

In renderdoc select the file as the executable path. Remember to set the correct working directory.

Note: you'll have to update the file for every minecraft version / dependency change