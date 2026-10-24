"""Report directed gate evidence locally; never uploads artifacts or runs tests."""
import os
from pathlib import Path
import xml.etree.ElementTree as ET

GROUPS = ('ui', 'd28', 'capabilities', 'spotify', 'cast', 'imports', 'metrics', 'sources')


def summarize(evidence, source_sha, compile_result, groups_result):
    lines = [
        '### Spotlight directed-tests',
        f'Source: `{source_sha}`',
        f'Compilation: **{compile_result}**; directed step: **{groups_result}**.',
        'Artifact upload temporarily disabled. Full output remains in the Actions job logs.',
        '',
        '| Group | Execution | Passed | Failed | Errors | Skipped |',
        '| --- | --- | ---: | ---: | ---: | ---: |',
    ]
    invalid = False
    for group in GROUPS:
        exit_file = evidence / f'{group}.exit-code'
        started = (evidence / f'{group}.started').exists()
        status = 'not started'
        if exit_file.exists():
            code = exit_file.read_text().strip()
            status = 'success (exit 0)' if code == '0' else f'failed (exit {code})'
        elif started:
            status = 'started; no exit status (interrupted)'
        reports = sorted((evidence / 'results' / group).glob('*.xml'))
        counts = ['—'] * 4
        if reports:
            try:
                totals = [0] * 4
                for report in reports:
                    root = ET.parse(report).getroot()
                    suites = [root] if root.tag == 'testsuite' else list(root.iter('testsuite'))
                    if not suites:
                        raise ValueError('missing testsuite')
                    for suite in suites:
                        tests, failures, errors, skipped = (
                            int(suite.attrib.get(key, '0'))
                            for key in ('tests', 'failures', 'errors', 'skipped')
                        )
                        passed = tests - failures - errors - skipped
                        values = [passed, failures, errors, skipped]
                        if min(values) < 0:
                            raise ValueError('invalid test counts')
                        totals = [a + b for a, b in zip(totals, values)]
                counts = list(map(str, totals))
            except (ET.ParseError, OSError, ValueError):
                status += '; invalid XML (counts unavailable)'
                invalid = True
        else:
            status += '; no XML'
        lines.append(f'| {group} | {status} | ' + ' | '.join(counts) + ' |')
    lines += ['', 'Counts describe XML-reported cases, not physical device validation.']
    return '\n'.join(lines) + '\n', invalid


def main():
    text, invalid = summarize(Path('gate-evidence'), os.environ['SOURCE_SHA'],
                              os.environ.get('COMPILE_RESULT', 'unknown'),
                              os.environ.get('GROUPS_RESULT', 'unknown'))
    print(text, end='')
    with Path(os.environ['GITHUB_STEP_SUMMARY']).open('a') as summary:
        summary.write(text)
    return 1 if invalid else 0


if __name__ == '__main__':
    raise SystemExit(main())
