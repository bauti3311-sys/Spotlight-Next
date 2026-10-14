#!/usr/bin/env python3
"""Manual scopes: tablet Android16 by default, legacy only when selected."""
import json
import os


def device_matrix(scope):
    tablet = [{'profile': 'pixel_c', 'api': 36}]
    legacy = [{'profile': profile, 'api': api}
              for api in (29, 30) for profile in ('pixel_6', 'pixel_c')]
    scopes = {'android16-tablet': tablet, 'legacy-complete': legacy, 'complete': tablet + legacy}
    if scope not in scopes:
        raise ValueError('Unknown device scope; refusing to start a matrix')
    return {'include': scopes[scope]}


if __name__ == '__main__':
    value = json.dumps(device_matrix(os.environ.get('SPOTLIGHT_DEVICE_SCOPE', 'android16-tablet')))
    with open(os.environ['GITHUB_OUTPUT'], 'a') as output:
        output.write(f'matrix={value}\n')
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as summary:
        summary.write(f'Manual device scope: `{os.environ["SPOTLIGHT_DEVICE_SCOPE"]}`\n\n`{value}`\n')
