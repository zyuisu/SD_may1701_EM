set SERVICE_NAME=EarthModellingDaemonMaster
set PR_DISPLAYNAME=EarthModellingDaemonMaster
set PR_INSTALL=C:\procrun\amd64\prunsrv.exe
 
REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=c:\procrun\logs
set PR_STDOUTPUT=c:\procrun\logs\stdout.txt
set PR_STDERROR=c:\procrun\logs\stderr.txt
set PR_LOGLEVEL=ERROR
 
REM Path to java installation
set PR_JVM=C:\Program Files\Java\jre1.8.0_121\bin\server\jvm.dll
set PR_CLASSPATH=daemon.jar
 
REM Startup configuration
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=main.EarthModellingDaemon
set PR_STARTMETHOD=main
set PR_STARTPARAMS=startOrStop;keystorePassword;arcgisServerUsername;arcgisServerPassword;htmlServerUsername;htmlServerPassword
 
REM Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=main.EarthModellingDaemon
set PR_STOPMETHOD=main
set PR_STOPPARAMS=stop
 
REM JVM configuration
set PR_JVMMS=256
set PR_JVMMX=4096
set PR_JVMSS=1024
set PR_JVMOPTIONS=-Duser.language=EN;-Duser.region=us

REM Install service
prunsrv.exe //IS//%SERVICE_NAME% --Startup=auto