#!/usr/bin/env python



def main(argv = sys.argv):
	JSON = '{"token" : '

    # Get file to parse
    f = open argv[0]

    # Get token
    token = argv[1]
    JSON += '"' + token '", "attributes" : ['
    
    try:
    	lines = [line.strip() for line in f]
    	for(attr in lines[0].split(","):
    		JSON += '" ' + attr + ',"
    	JSON = JSON[:-1]
    	JSON += '], "examples" : [ 
	finally:
    	f.close()



if __name__ == "__main__":
    try:
        main(sys.argv)
    except KeyboardInterrupt:
        pass