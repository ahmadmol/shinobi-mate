"""Compute comparable per-phase metrics from raw process counters, no simulated ticks."""
import csv,json,pathlib
root=pathlib.Path(__file__).resolve().parents[1]/'output/performance'
all_results={}
for variant in ['before','after']:
    results={}
    for file in sorted((root/variant).glob('*.csv')):
        rows=list(csv.DictReader(file.open()))
        a,b=rows[0],rows[-1]; elapsed=float(b['seconds'])-float(a['seconds'])
        latency=sorted(float(r['edtMillis']) for r in rows)
        results[file.stem]={'seconds':elapsed,'ticks_per_second':(int(b['ticks'])-int(a['ticks']))/elapsed,
            'cpu_logical_cores':(int(b['cpuNanos'])-int(a['cpuNanos']))/1e9/elapsed,
            'heap_peak_MiB':max(int(r['heapBytes']) for r in rows)/1048576,
            'thread_min':min(int(r['threads']) for r in rows),'thread_max':max(int(r['threads']) for r in rows),
            'edt_max_ms':max(latency),'edt_p95_ms':latency[min(len(latency)-1,int(.95*len(latency)))],
            'audio_voices_peak':max(int(r['voices']) for r in rows)}
    all_results[variant]=results
(root/'benchmarks.json').write_text(json.dumps(all_results,indent=2),encoding='utf-8')
print('| Phase | Before Hz | After Hz | CPU before | CPU after | CPU reduction | EDT max before/after ms | Heap peak before/after MiB |')
print('|---|---:|---:|---:|---:|---:|---|---|')
for phase,b in all_results['before'].items():
    a=all_results['after'][phase]
    print(f"|{phase}|{b['ticks_per_second']:.3f}|{a['ticks_per_second']:.3f}|{b['cpu_logical_cores']:.3f}|{a['cpu_logical_cores']:.3f}|{100*(1-a['cpu_logical_cores']/b['cpu_logical_cores']):.1f}%|{b['edt_max_ms']:.3f} / {a['edt_max_ms']:.3f}|{b['heap_peak_MiB']:.1f} / {a['heap_peak_MiB']:.1f}|")
