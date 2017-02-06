
Configuration:
	Windows 7
	Java 7 64-bit
	ProM 6.5
	

To install the package:

1. Copy folder 'overallprocessrisk-6' to ProM packages folder.
2. Add to packages.xml under <installed-packages> entry:

<package name="overallprocessrisk" version="6" os="all" url="http://www.yawlfaundation.org" desc="overallprocessrisk" org="QUT" auto="false" hasPlugins="true" license="LGPL" author="A. Pika" maintainer="A. Pika" logo="">
 </package>

3. Create system variable R_HOME with the value:
[Prom packages path]\overallprocessrisk-6\OverallProcessRisk_lib\R-3.0.0

4. Add to system Path variable the value: 
[Prom packages path]\overallprocessrisk-6\OverallProcessRisk_lib\jrilib

5. Restart Windows

Troubleshooting:

In case of an exception, remove package 'DataAwareReplayer' if it is installed, clean ProM's cache and restart ProM