-- Synthetic CI cold invocation only. No query text, files or credentials.
SELECT p.name AS process, t.name AS thread, s.name AS section,
       ROUND((s.ts - b.start_ts) / 1000000.0, 2) AS start_ms,
       ROUND(s.dur / 1000000.0, 2) AS duration_ms
FROM slice s
JOIN thread_track tr ON tr.id = s.track_id
JOIN thread t ON t.utid = tr.utid
JOIN process p ON p.upid = t.upid
CROSS JOIN trace_bounds b
WHERE p.name LIKE '%spotlight%' AND s.dur > 5000000
ORDER BY s.dur DESC LIMIT 30;

SELECT p.name AS process, t.name AS thread,
       ROUND(SUM(s.dur) / 1000000.0, 2) AS running_cpu_ms
FROM sched s JOIN thread t ON t.utid = s.utid
JOIN process p ON p.upid = t.upid
WHERE p.name LIKE '%spotlight%'
GROUP BY t.utid ORDER BY running_cpu_ms DESC LIMIT 12;
