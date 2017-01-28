
Configuration:
	Windows 7
	Java 7 64-bit
	ProM 6.5
	MySQL Server 5.7

To install the package:

1. Copy folder 'overallprocessrisk-6.5' to ProM packages folder.
2. Add to packages.xml under <installed-packages> entry:

<package name="overallprocessrisk" version="6.5" os="all" url="http://www.yawlfaundation.org" desc="OverallProcessRisk" org="QUT" auto="false" hasPlugins="true" license="LGPL" author="A. Pika" maintainer="A. Pika" logo="">
 </package>

3. Create system variable R_HOME with the value:
[Prom packages path]\overallprocessrisk-6.5\OverallProcessRisk_lib\R-3.0.0

4. Add to system Path variable the value: 
[Prom packages path]\overallprocessrisk-6.5\OverallProcessRisk_lib\jrilib

5. Restart Windows