How to integrate into your project
==================================

1. clone the repo and put it into your plugins folder.
2. modify the project pom.xml to include it

	```
	<modules>
		...
		<module>plugins/audio-plugin</module>
	</modules>


	<dependencies>
	    ...
        <dependency>
	      <groupId>com.atex.plugins</groupId>
	      <artifactId>audio</artifactId>
	      <version>2.3</version>
	    </dependency>
	    
	    <dependency>
	      <groupId>com.atex.plugins</groupId>
	      <artifactId>audio</artifactId>
	      <version>2.3</version>
	      <classifier>contentdata</classifier>
	    </dependency>
	    ...
	</dependencies>
	```
