def assemble_entity(record):
    entity = {
        "business_id": str(record[0]).replace("'", ""),
        "name": str(record[1]).replace("'", ""),
        "city": str(record[3]).replace("'", ""),
        "state": str(record[4]),
        "stars": float(record[6]),
        "review": int(record[7]),
        "favorites": int(record[8]),
        "avgspend": int(record[9]),
        "popular": bool(record[10]),
        "categories": record[11],
        "feature": eval(record[12])
    }
    return entity
