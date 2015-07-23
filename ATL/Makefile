JAVA_FILES = $(shell find src/ -type f -name '*.java')

src.xmi: $(JAVA_FILES)
	java -Xmx1G -jar jamopp/jamoppc.jar --disable-layout src/ src.xmi lib/slf4j-api-1.6.4.jar
