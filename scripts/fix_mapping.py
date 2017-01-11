# python scripts/fix_mapping.py -e http://localhost:9200 -i oerworldmap -o conf/index-config.json

__author__ = 'fo'

import sys, getopt, json, os, urllib2


def fix(endpoint_url, index_name, output_file):
    mapping = fix_mappings(get_mapping(endpoint_url, index_name))
    with open(output_file, 'w') as fixed_mapping:
        json.dump(mapping[index_name], fixed_mapping, indent=2, separators=(',', ': '))


def fix_mappings(json):
    for index in json:
        json[index] = process_index(json[index])
        json[index]["settings"] = settings()
    return json


def process_index(index):
    for mapping in index:
        index[mapping] = process_mapping(index[mapping])
    return index


def process_mapping(mapping):
    for properties in mapping:
        mapping[properties]['properties'] = process_properties(mapping[properties]['properties'])
        mapping[properties]['transform'] = transform()
    return mapping


def process_properties(properties):
    not_analyzed = ['@id', '@type', '@context', '@language', 'addressCountry', 'email', 'url', 'image', 'keywords',
                    'availableLanguage', 'prefLabel', 'postalCode', 'hashtag', 'addressRegion']
    ngrams = ['@value']
    date_time = ['startDate', 'endDate', 'startTime', 'endTime', 'dateCreated', 'hasAwardDate']
    geo = ['geo']
    for property in properties:
        if property in not_analyzed:
            properties[property] = set_not_analyzed(properties[property])
        elif property in date_time:
            properties[property] = set_date_time()
        elif property in geo:
            properties[property] = set_geo_point()
        elif property in ngrams:
            properties[property] = set_ngram()
        elif 'properties' in properties[property]:
            properties[property]['properties'] = process_properties(properties[property]['properties'])

    return properties

def set_not_analyzed(field):
    field['type'] = 'string'
    field['index'] = 'not_analyzed'
    return field

def set_date_time():
    return {
        'type': 'date',
        'format': 'dateOptionalTime'
    }

def set_geo_point():
    return {
        "type": "geo_point"
    }

def set_ngram():
    return {
        "type": "multi_field",
        "fields": {
            "@value": {
                "type": "string"
            },
            "variations": {
                "type": "string",
                "analyzer": "title_analyzer",
                "search_analyzer": "title_analyzer"
            },
            "simple_tokenized": {
                "type": "string",
                "analyzer": "simple",
                "search_analyzer": "standard"
            }
        }
    }

def transform():
    return {
        "script": """
            if (!ctx._source['about']['location']) {

                ctx._source['about']['location'] = [];

                if (ctx._source['about']['provider'] && ctx._source['about']['provider']['location'])
                    ctx._source['about']['location'] << ctx._source['about']['provider']['location'];

                if (ctx._source['about']['agent'] && ctx._source['about']['agent']['location'])
                    ctx._source['about']['location'] << ctx._source['about']['agent']['location'];

                if (ctx._source['about']['participant'] && ctx._source['about']['participant']['location'])
                    ctx._source['about']['location'] << ctx._source['about']['participant']['location'];

                if (ctx._source['about']['member'] && ctx._source['about']['member']['location'])
                    ctx._source['about']['location'] << ctx._source['about']['member']['location'];

                if (ctx._source['about']['mentions'] && ctx._source['about']['mentions']['location'])
                    ctx._source['about']['location'] << ctx._source['about']['mentions']['location'];
            };
        """
    }

def settings():
    return {
        "analysis": {
            "filter": {
                "title_filter": {
                    "type": "word_delimiter",
                    "preserve_original": True,
                    "split_on_numerics": False,
                    "split_on_case_change": True,
                    "generate_word_parts": True,
                    "generate_number_parts": False,
                    "catenate_words": True,
                    "catenate_numbers": False,
                    "catenate_all": False
                },
                "asciifolding_preserve_original": {
                    "type": "asciifolding",
                    "preserve_original": True
                },
                "autocomplete_filter": {
                    "type":     "edge_ngram",
                    "min_gram": 2,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "title_analyzer": {
                    "filter": [
                        "title_filter",
                        "asciifolding_preserve_original",
                        "autocomplete_filter",
                        "lowercase"
                    ],
                    "type": "custom",
                    "tokenizer": "hyphen"
                }
            }
        }
    }

def get_mapping(endpoint, index):
    response = urllib2.urlopen(endpoint + '/' + index + '/_mapping')
    return json.loads(response.read())


if __name__ == "__main__":
    endpoint_url = ''
    index_name = ''
    output_file = ''
    try:
        opts, args = getopt.getopt(sys.argv[1:], "he:i:o:", ["endpoint_url=", "output_file="])
    except getopt.GetoptError:
        print '1.py -e <endpoint_url> -i <index_name> -o <output_file>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print '1.py -e <endpoint_url> -i <index_name> -o <output_file>'
            sys.exit()
        elif opt in ("-e", "--endpoint_url"):
            endpoint_url = arg
        elif opt in ("-i", "--index_name"):
            index_name = arg
        elif opt in ("-o", "--output_file"):
            output_file = arg
    fix(endpoint_url, index_name, output_file)
