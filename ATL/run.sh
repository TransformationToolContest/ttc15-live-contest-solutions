#!/bin/sh

JAVA="/cygdrive/c/Program Files/Java/jre7/bin/java.exe"
if [ -f "$JAVA" ] ; then
	# TODO: make the alias available in Makefile
	alias java="${JAVA// /\\ }"
fi

# Parsing all Java source files (this only needs to be done when these files have changed, not for every execution of the transformation):
# DONE: use files timestamps to reparse only when necessary (e.g., using a Makefile)
#java -Xmx1G -jar jamopp/jamoppc.jar --disable-layout src/ src.xmi lib/slf4j-api-1.6.4.jar
make src.xmi

# Transforming & serializing
java -cp "jamopp/jamoppc.jar;atl/libs/*;atl" Exec "$@"

