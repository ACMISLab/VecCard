s = "review.(read = FALSE and ((stars>=4.0 and funny=5.0) or (cool>26.0 and dislikes>=9 and views>12)))"

keywords = []
i = 0
while i < len(s):
    if s[i:i+4] == " and":
        keywords.append(" and")
        i +=4
    elif s[i:i+3] == " or":
        keywords.append(" or")
        i +=3
    else:
        i +=1

print(keywords)