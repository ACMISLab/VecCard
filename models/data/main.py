import shelve
import numpy as np

tables = ["business", "review", "tip", "problem", "yelp_user", "mcx_user", "db_comments", "db_movies"]
tables_row = [150243, 4000000, 851033, 125385, 331244, 45316, 4166705, 131442]
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
    "problem.language": {
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
    },
    "mcx_user.gender": {
        "type": "VARCHAR",
        "value": [0.0, 1.0, 2.0],
        "max_card": 21589,
        "min_card": 11131
    },
    "mcx_user.school": {
        "type": "VARCHAR",
        "value": ['三亚学院', '三亚理工职业学院', '三峡大学', '东北大学', '东北电力大学', '东南大学', '东莞理工学院城市学院', '中国农业大学', '中国矿业大学银川学院', '云南大学', '云南大学滇池学院', '云南师范大学文理学院', '云南财经大学', '兰州交通大学', '兰州财经大学', '内蒙古农业大学', '内蒙古大学', '内蒙古师范大学', '北京中医药大学', '北京建筑大学', '北京航空航天大学', '北华大学', '华南农业大学', '华南理工大学', '南京邮电大学', '南京邮电大学通达学院', '南开大学滨海学院', '右江民族医学院', '吉首大学', '哈尔滨工业大学', '哈尔滨工业大学（威海）', '哈尔滨金融学院', '四川中医药高等专科学校', '四川农业大学', '大连医科大学', '大连医科大学中山学院', '大连外国语大学', '大连工业大学', '大连民族大学', '大连海事大学', '大连理工大学', '天津商业大学', '太原师范学院', '宁夏大学', '宜宾学院', '山东工商学院', '岭南师范学院', '广东工业大学', '广东海洋大学寸金学院', '广东财经大学', '广州中医药大学', '广州大学', '广西科技大学', '德州学院', '扬州大学', '新疆农业大学', '新疆师范大学', '昆明医科大学海源学院', '昆明学院', '昆明理工大学', '昆明理工大学城市学院', '昆明理工大学津桥学院', '暨南大学', '桂林电子科技大学', '武汉商学院', '江苏大学', '江西理工大学', '沈阳工学院', '沈阳建筑大学', '河北民族师范学院', '清华大学', '湖北职业技术学院', '湖南城市学院', '湖南大学', '潍坊医学院', '烟台大学', '燕山大学', '甘肃中医药大学', '百色学院', '空军工程大学', '绥化学院', '芜湖职业技术学院', '西北政法大学', '西华大学', '西南科技大学', '西安工程大学', '西安科技大学', '贵州医科大学', '贵州理工学院', '辽宁对外经贸学院', '辽宁工程技术大学', '辽宁石油化工大学', '邢台学院', '重庆大学', '陕西工业职业技术学院', '陕西科技大学', '集宁师范学院', '青岛理工大学', '青岛科技大学', '青海大学', '鞍山师范学院', '首都医科大学', '鲁迅美术学院'],
        "max_card": 999,
        "min_card": 100
    },
    "mcx_user.year": {
        "type": "INT",
        "value": [1970, 2020],
        "max_card": 43557,
        "min_card": 11
    },
    "yelp_user.review": {
            "type": "INT",
            "value": [18, 326],
            "max_card": 417,
            "min_card": 100
    },
    "yelp_user.useful": {
            "type": "INT",
            "value": [28, 346],
            "max_card": 291,
            "min_card": 100
    },
    "yelp_user.funny": {
        "type": "INT",
        "value": [5, 187],
        "max_card": 1017,
        "min_card": 100
    },
    "yelp_user.cool": {
        "type": "INT",
        "value": [5, 239],
        "max_card": 645,
        "min_card": 100
    },
    "yelp_user.fans": {
        "type": "INT",
        "value": [5, 72],
        "max_card": 11688,
        "min_card": 105
    },
    "db_comments.vote": {
        "type": "INT",
        "value": [0, 202],
        "max_card": 102,
        "min_card": 3367758
    },
    "db_comments.rating": {
        "type": "FLOAT",
        "value": [1.0, 5.0],
        "max_card": 318187,
        "min_card": 1402894
    },
    "db_comments.year": {
        "type": "INT",
        "value": [2005, 2019],
        "max_card": 1414,
        "min_card": 898432
    },
    "db_movies.score": {
        "type": "FLOAT",
        "value": [0.0, 9.5],
        "max_card": 18,
        "min_card": 105611
    },
    "db_movies.vote": {
        "type": "INT",
        "value": [0, 192],
        "max_card": 50,
        "min_card": 93088
    },
    "db_movies.language": {
        "type": "VARCHAR",
        "value": ['Albanian', 'Bengali', 'English', 'Hindi', 'Icelandic', 'Kannada', 'Malayalam', 'Marathi', 'Panjabi', 'Punjabi', 'Serbian', 'Silent', 'Tamil', 'Telugu', 'silent', '中文', '丹麦语', '丹麦语 Danish', '乌克兰语', '乌尔都语', '依地语', '俄罗斯语', '俄語', '俄语', '俄语 Russian', '保加利亚语', '冰岛语', '加泰罗尼亚语', '匈牙利语', '北印度语', '北印度语 Hindi', '卡纳达语', '印地语', '印地语 Hindi', '印度语', '印度语 Hindi', '台语', '哈萨克语', '国语', '土耳其语', '坎纳达语', '塞尔维亚语', '孟加拉语', '巴斯克语', '希伯来语', '希腊语', '德语', '意大利语', '挪威语', '捷克语', '斯洛伐克语', '旁遮普语', '无声', '无对白', '日语', '普通话', '朝鲜语', '格鲁吉亚语', '汉语普通话', '法语', '波兰语', '波斯尼亚语', '波斯语', '波斯语 Persian', '泰卢固语', '泰卢固语 Telugu', '泰米尔语', '泰米尔语 Tamil', '泰语', '潮语', '瑞典语', '瑞典语 Swedish', '瑞士德语', '粤语', '罗马尼亚语', '芬兰语', '英語 English', '英语', '荷兰语', '菲律宾语', '葡萄牙语', '葡萄牙语 Portuguese', '蒙古语', '藏语', '西班牙语', '越南语', '闽南话', '闽南语', '阿尔巴尼亚语', '阿拉伯语', '韩语', '马拉地语', '马拉雅拉姆语', '马拉雅拉姆语 Malayalam', '默片'],
        "max_card": 10,
        "min_card": 53841
    },
    "db_movies.mins": {
        "type": "FLOAT",
        "value": [0.0, 200.0],
        "max_card": 50,
        "min_card": 39993
    },
    "db_movies.region": {
        "type": "VARCHAR",
        "value": ['Bulgaria', 'Chile', 'Cuba', 'Czechoslovakia', 'Estonia', '中国', '中国台湾', '中国大陆', '中国香港', '丹麦', '丹麦 Denmark', '乌克兰', '乌拉圭', '以色列', '伊朗', '伊朗 Iran', '俄罗斯', '俄罗斯 Russia', '俄羅斯', '保加利亚', '冰岛', '加拿大', '加拿大 Canada', '匈牙利', '南斯拉夫', '南非', '印度', '印度 India', '印度 Indian', '印度 india', '古巴', '台湾', '哈萨克斯坦', '哥伦比亚', '土耳其', '塞尔维亚', '墨西哥', '奥地利', '委内瑞拉', '巴西', '巴西 Brazil', '希腊', '德国', '意大利', '挪威', '捷克', '捷克斯洛伐克', '新加坡', '新西兰', '日本', '智利', '格鲁吉亚', '比利时', '法国', '波兰', '泰国', '澳大利亚', '澳大利亚 Australia', '爱尔兰', '爱尔兰 Ireland', '瑞典', '瑞典 Sweden', '瑞士', '秘鲁', '罗马尼亚', '美国', '芬兰', '苏联', '英国', '荷兰', '菲律宾', '葡萄牙', '西德', '西班牙', '越南', '阿根廷', '韩国', '香港', '黎巴嫩'],
        "max_card": 10,
        "min_card": 37342
    },
    "db_movies.year": {
        "type": "INT",
        "value": [0, 2022],
        "max_card": 38,
        "min_card":  5534
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


with shelve.open('./metadata/metadata-ewc.db') as db:
    db['tables'] = tables
    db['tables_row'] = tables_row
    db['columns'] = handleCard(columns)
