#!/usr/bin/env python
import sys


def toJSON(f, token, query):
    try:
        file = open(f)
        lines = [line.strip() for line in file]
        attributes = "["
        examples = "["
        for attr in lines[0].split(","):
            attributes += '"' + attr + '", '

        for i in range(1, len(lines)):
            examples += '"' + lines[i] + '", '

        examples = examples[:-2]
        examples += "]"
        attributes = attributes[:-2]
        attributes += "]"
        if(query):
            return '{{"token" : {0}, "examples" : {1}}}'.format('"' + token + '"', examples)
        else:
            return '{{"token" : {0}, "attributes" : {1}, "examples" : {2}}}'.format('"' + token + '"', attributes, examples)

    finally:
        file.close()

def main(argv = sys.argv):
    if(len(argv) < 3):
        print "Usage: python CSVtoJSON.py [token] [path/to/csv/data]"
        return
    elif(len(argv) > 3):
        if(argv[3] == "-query"):
            print(toJSON(argv[1], argv[2], True))
            return

    print(toJSON(argv[1], argv[2], False))

if __name__ == "__main__":
    try:
        main(sys.argv)
    except KeyboardInterrupt:
        pass