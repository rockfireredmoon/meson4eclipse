project($(projectName), 'c')

libExample = library($(projectName), ['$(libName).c'])
libInclude = include_directories('include')

executable($(projectName), 'exampleProgram/exampleProgram.c', link_with: libExample, include_directories: libInclude)