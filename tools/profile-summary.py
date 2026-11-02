"""Summarize JFR JSON samples without confusing native waits with CPU time."""
import collections, json, pathlib, sys
for arg in sys.argv[1:]:
    data = json.loads(pathlib.Path(arg).read_text(encoding='utf-8-sig'))
    threads, leaves, app, stacks = [collections.Counter() for _ in range(4)]
    for event in data['recording']['events']:
        v=event['values']; thread=(v.get('sampledThread') or {}).get('javaName','?')
        frames=(v.get('stackTrace') or {}).get('frames',[])
        names=[f['method']['type']['name'].replace('/','.')+'.'+f['method']['name'] for f in frames]
        if not names: continue
        threads[thread]+=1
        if thread!='ShinobiMate engine': continue
        leaves[names[0]]+=1
        for name in set(names):
            if name.startswith('com.group_finity.'): app[name]+=1
        stacks[' <- '.join(names[:8])]+=1
    print('\nFILE',arg,'\nTHREADS',threads.most_common())
    for label,counter in [('ENGINE LEAF',leaves),('ENGINE INCLUSIVE APP',app),('ENGINE STACKS',stacks)]:
        print(label)
        for name,n in counter.most_common(16): print(n,name)
