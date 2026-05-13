import shelve
import numpy as np
import os

tables = ["business", "review", "tip", "problem"]
tables_row = [150243, 4000000, 851033, 125385]
columns = {
    "business.city": {
        "type": "VARCHAR",
        "value": ["St Petersburg", "Meridian", "Moorestown", "Temple Terrace", "Sicklerville", "Narberth",
                  "Williamstown", "Clementon", "King Of Prussia", "Warrington", "Mount Holly", "Abington",
                  "Sherwood Park", "Morrisville", "New Port Richey", "Doylestown", "Des Peres", "Phoenixville",
                  "Riverview", "Newtown Square", "Florissant", "Plant City", "Swedesboro", "Brandon", "Brownsburg",
                  "Chalmette", "Deptford", "Apollo Beach", "Collinsville", "Seffner", "Fairless Hills", "West Chester",
                  "Blue Bell", "Bryn Mawr", "Edwardsville", "Collingswood", "Sewell", "Perkasie", "Clayton",
                  "Kennett Square", "Cherry Hill", "Tampa", "Palm Harbor", "Berwyn", "New Orleans", "Kirkwood",
                  "Santa Barbara", "Lambertville", "Madison", "Mount Laurel", "Maryland Heights", "Trinity",
                  "Tampa Bay", "Delran", "Goleta", "Downingtown", "Maple Shade", "Blackwood", "Nashville", "Gretna",
                  "Ewing", "Paoli", "Newark", "Manchester", "Franklin", "Bala Cynwyd", "Lutz", "Saint Charles",
                  "St Charles", "Collegeville", "Indianapolis", "Drexel Hill", "King of Prussia", "Malvern", "Bristol",
                  "Hazelwood", "New Hope", "Alton", "Garden City", "Ardmore", "Gallatin", "Mount Juliet", "Kenner",
                  "Chalfont", "Metairie", "Broomall", "Marlton", "Marana", "Zephyrhills", "Jenkintown",
                  "Hendersonville", "Willow Grove", "Glassboro", "Land O Lakes", "Harleysville", "Upper Darby",
                  "Norristown", "Odessa", "Goodlettsville", "Arnold", "Media", "Trenton", "Lansdale", "University City",
                  "Oro Valley", "Zionsville", "Bridgeton", "Smyrna", "Hudson", "Audubon", "Madeira Beach", "Souderton",
                  "Wayne", "Harahan", "Belleville", "Holiday", "Spring Hill", "Clearwater Beach", "Aston",
                  "Elkins Park", "New Castle", "Nolensville", "Royersford", "Montecito", "St. Charles", "Yardley",
                  "Fishers", "Horsham", "Hatboro", "Wynnewood", "St Louis", "Warminster", "Richmond Heights", "Ruskin",
                  "Pottstown", "Fairview Heights", "Wesley Chapel", "Richboro", "Avon", "Haddonfield", "Havertown",
                  "St Albert", "Hatfield", "Carpinteria", "Safety Harbor", "Port Richey", "Gulfport", "Pennsauken",
                  "Newtown", "Burlington", "Tucson", "Berlin", "Southampton", "Edmonton", "Bordentown",
                  "Webster Groves", "Plainfield", "Tarpon Springs", "Saint Louis", "Camden", "Feasterville Trevose",
                  "Plymouth Meeting", "Glenside", "Huntingdon Valley", "Eagle", "Ambler", "St. Petersburg",
                  "Isla Vista", "Chadds Ford", "Woodbury", "St. Pete Beach", "Carmel", "Philadelphia", "Voorhees",
                  "Creve Coeur", "Ballwin", "Montgomeryville", "Bensalem", "Valrico", "Saint Petersburg", "Dunedin",
                  "Sparks", "Fenton", "Levittown", "Greenwood", "Glen Mills", "Brentwood", "Conshohocken", "Antioch",
                  "Harvey", "Boise", "La Vergne", "Pinellas Park", "Largo", "North Wales", "Clearwater", "Marrero",
                  "Langhorne", "Reno", "Turnersville", "Cinnaminson", "Hermitage", "Seminole", "Wilmington",
                  "St. Louis", "Treasure Island", "O Fallon", "Springfield", "Maplewood", "Exton", "Oldsmar", "Medford",
                  "East Norriton", "Chesterfield", "OFallon", "Folsom"],
        "max_card": 14560,
        "min_card": 86
    },
    "business.state": {
        "type": "VARCHAR",
        "value": ["AB", "MO", "IL", "IN", "PA", "LA", "AZ", "TN", "NJ", "DE", "FL", "NV", "ID", "CA"],
        "max_card": 34013,
        "min_card": 2144
    },
    "business.stars": {
        "type": "FLOAT",
        "value": [0.0, 5.0],
        "max_card": 31120,
        "min_card": 1948
    },
    "business.review": {
        "type": "INT",
        "value": [5, 252],
        "max_card": 14872,
        "min_card": 36
    },
    "business.favorites": {
        "type": "INT",
        "value": [0, 220],
        "max_card": 1346,
        "min_card": 580
    },
    "business.avgspend": {
        "type": "INT",
        "value": [10, 160],
        "max_card": 3650,
        "min_card": 496
    },
    "business.popular": {
        "type": "BOOL",
        "value": [0.6704205853, 0.3295794147],
        "max_card": 100726,
        "min_card": 49517
    },
    "review.stars": {
        "type": "FLOAT",
        "value": [1.0, 5.0],
        "max_card": 2132694,
        "min_card": 242821
    },
    "review.useful": {
        "type": "FLOAT",
        "value": [-1, 404],
        "max_card": 2604855,
        "min_card": 11
    },
    "review.funny": {
        "type": "FLOAT",
        "value": [-1, 345],
        "max_card": 3590968,
        "min_card": 16
    },
    "review.cool": {
        "type": "FLOAT",
        "value": [-1, 404],
        "max_card": 3325384,
        "min_card": 10
    },
    "review.likes": {
        "type": "INT",
        "value": [0, 150],
        "max_card": 50948,
        "min_card": 6291
    },
    "review.dislikes": {
        "type": "INT",
        "value": [0, 48],
        "max_card": 211372,
        "min_card": 36644
    },
    "review.views": {
        "type": "INT",
        "value": [0, 20],
        "max_card": 330222,
        "min_card": 82753
    },
    "review.read": {
        "type": "BOOL",
        "value": [0.7053295, 29.46705],
        "max_card": 2821318,
        "min_card": 1178682
    },
    "tip.compliment": {
        "type": "INT",
        "value": [0, 6],
        "max_card": 840795,
        "min_card": 2
    },
    "tip.amount": {
        "type": "FLOAT",
        "value": [2, 20],
        "max_card": 4946,
        "min_card": 2340
    },
    "tip.num": {
        "type": "INT",
        "value": [1, 213],
        "max_card": 739837,
        "min_card": 10
    },
    "problem.lang": {
        "type": "BOOL",
        "value": [0.894644495, 0.105355505],
        "max_card": 112175,
        "min_card": 13210
    },
    "problem.score": {
        "type": "VARCHAR",
        "value": [0.0, 0.5, 0.8, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 12.5, 15.0,
                  20.0, 25.0, 30.0, 33.0, 40.0, 50.0, 100.0],
        "max_card": 94453,
        "min_card": 10
    },
    "problem.type": {
        "type": "VARCHAR",
        "value": [1, 2, 3, 4, 5, 6],
        "max_card": 69088,
        "min_card": 218
    }
}


def handleCard(columns):
    column_keys = list(columns.keys())
    cards = []
    for key in column_keys:
        column = columns[key]
        cards.append(column["max_card"])
        cards.append(column["min_card"])
    max_card = np.array(cards).max()
    min_card = np.array(cards).min()
    # 归一化处理（Min-Max 归一化）
    min_val = np.log1p(min_card)
    max_val = np.log1p(max_card)
    for key in column_keys:
        column = columns[key]
        max_card = np.log1p(column["max_card"])
        min_card = np.log1p(column["min_card"])
        column["max_card"] = (max_card - min_val) / (max_val - min_val)
        column["min_card"] = (min_card - min_val) / (max_val - min_val)
    return columns


# db_path = './metadata/metadata-dingo.db'
#
# # 确保目录存在
# os.makedirs(os.path.dirname(db_path), exist_ok=True)

with shelve.open('./metadata/metadata-dingo.db') as db:
    db['tables'] = tables
    db['tables_row'] = tables_row
    db['columns'] = handleCard(columns)
