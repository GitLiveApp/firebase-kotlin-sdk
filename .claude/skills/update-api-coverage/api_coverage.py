#!/usr/bin/env python3
"""Recalculate the "API Coverage" badges in README.md.

Coverage for a module is the number of public, non-deprecated firebase-android-sdk
API members (from the module's api.txt on the main branch) that this SDK's
androidMain sources invoke, divided by the total number of such members.

Usage (from the repository root):
    python3 .claude/skills/update-api-coverage/api_coverage.py            # print the table
    python3 .claude/skills/update-api-coverage/api_coverage.py --write    # also update README.md
    python3 .claude/skills/update-api-coverage/api_coverage.py -v         # per-class detail
    python3 .claude/skills/update-api-coverage/api_coverage.py --exclude-pipeline
    python3 .claude/skills/update-api-coverage/api_coverage.py --api-dir DIR  # use local api.txt files
"""
import argparse
import collections
import glob
import os
import re
import sys
import urllib.request

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..'))
API_URL = 'https://raw.githubusercontent.com/firebase/firebase-android-sdk/main/firebase-{}/api.txt'

# api.txt module name -> module directory in this repository.
# firebase-auth and firebase-analytics are closed source on Android and have no api.txt.
MODULES = {
    'database': 'firebase-database',
    'firestore': 'firebase-firestore',
    'functions': 'firebase-functions',
    'messaging': 'firebase-messaging',
    'storage': 'firebase-storage',
    'installations': 'firebase-installations',
    'config': 'firebase-config',
    'perf': 'firebase-perf',
    'crashlytics': 'firebase-crashlytics',
}
PIPELINE_PKGS = {'com.google.firebase.firestore.pipeline', 'com.google.firebase.firestore.pipeline.evaluation'}


def fetch_api(name, api_dir):
    if api_dir:
        return open(os.path.join(api_dir, f'{name}.txt')).read()
    with urllib.request.urlopen(API_URL.format(name)) as r:
        return r.read().decode()


def parse_api(text, exclude_pkgs=()):
    """Yield (package, class, kind, name) for every public, non-deprecated member."""
    pkg = cls = None
    cls_ok = False
    for line in text.splitlines():
        s = line.strip()
        m = re.match(r'package (\S+) \{', s)
        if m:
            pkg = m.group(1)
            continue
        m = re.match(r'((?:@\S+ )*)(public|protected) .*?(class|interface|enum|@interface) ([\w.]+)', s)
        if m and s.endswith('{'):
            cls = m.group(4)
            cls_ok = (m.group(2) == 'public' and '@Deprecated' not in m.group(1)
                      and '@RestrictTo' not in m.group(1) and m.group(3) != '@interface'
                      and pkg not in exclude_pkgs)
            continue
        m = re.match(r'(ctor|method|field|property|enum_constant) ((?:@\S+ )*)(public|protected) (.*);', s)
        if m and cls and cls_ok:
            kind, ann, vis, rest = m.groups()
            if vis != 'public' or '@Deprecated' in ann or '@RestrictTo' in ann:
                continue
            if kind == 'ctor':
                name = '<init>'
            else:
                decl = rest.split('(')[0] if kind == 'method' else rest.split(' = ')[0]
                name = decl.split()[-1]
                if name in ('Companion', 'INSTANCE'):
                    continue
            yield pkg, cls, kind, name


def android_sources(moddir):
    return glob.glob(os.path.join(ROOT, moddir, 'src/androidMain/**/*.kt'), recursive=True)


def public_typealiases(moddir):
    """Fully-qualified Android classes exposed wholesale through a public typealias."""
    out = set()
    for f in android_sources(moddir):
        text = open(f).read()
        imports = {}
        for m in re.finditer(r'^import\s+([\w.]+)(?:\s+as\s+(\w+))?', text, flags=re.M):
            imports[m.group(2) or m.group(1).split('.')[-1]] = m.group(1)
        for m in re.finditer(r'public\s+(?:actual\s+)?typealias\s+\w+(?:<[^>]*>)?\s*=\s*([\w.]+)', text):
            parts = m.group(1).split('.')
            fqn = m.group(1) if parts[0] == 'com' else '.'.join([imports.get(parts[0], parts[0])] + parts[1:])
            out.add(fqn)
    return out


def load_sources(moddir):
    src = '\n'.join(open(f).read() for f in android_sources(moddir))
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    src = re.sub(r'//[^\n]*', '', src)
    # Neutralise declarations so `fun foo(` / `val foo` do not count as invocations of foo.
    src = re.sub(r'\bfun\s+(<[^>]*>\s*)?([\w.<>?]+\.)?(\w+)\s*\(', r'fun \1\2__decl__(', src)
    src = re.sub(r'\b(val|var)\s+(<[^>]*>\s*)?([\w.<>?]+\.)?(\w+)\b', r'\1 \2\3__decl__', src)
    src = re.sub(r'\bclass\s+(\w+)', r'class __decl__', src)
    return src


def lower1(s):
    return s[0].lower() + s[1:]


def used(src, cls, kind, name):
    simple = cls.split('.')[-1]
    if kind == 'ctor':
        return re.search(r'(?<![\w.])' + re.escape(simple) + r'\s*\(', src) is not None
    if kind in ('field', 'enum_constant', 'property'):
        return re.search(r'(?<![\w])' + re.escape(name) + r'(?![\w(])', src) is not None
    if re.search(r'\b' + re.escape(name) + r'\s*[({]', src) or re.search(r'::' + re.escape(name) + r'\b', src):
        return True
    m = re.match(r'(get|is|set)([A-Z]\w*)$', name)
    if m:  # Java getter/setter used as a Kotlin property
        prop = name if m.group(1) == 'is' else lower1(m.group(2))
        if m.group(1) == 'set':
            return re.search(r'\.' + re.escape(prop) + r'\s*=[^=]', src) is not None
        return re.search(r'\.' + re.escape(prop) + r'(?![\w(])', src) is not None
    return False


def coverage(api, moddir, text, exclude_pipeline):
    exclude = PIPELINE_PKGS if exclude_pipeline else ()
    members = sorted(set(parse_api(text, exclude)))
    props = {(c, n) for _, c, k, n in members if k == 'property'}

    def synthetic(c, k, n):  # Kotlin property listed alongside its getter/setter
        m = re.match(r'(get|set|is)([A-Z]\w*)$', n)
        return k == 'method' and m and ((c, lower1(m.group(2))) in props or (m.group(1) == 'is' and (c, n) in props))

    members = [t for t in members if not synthetic(t[1], t[2], t[3])]
    if exclude_pipeline:
        members = [t for t in members if not t[1].split('.')[0].startswith('Pipeline')]
    src = load_sources(moddir)
    aliased = public_typealiases(moddir)
    has_pipeline = 'pipeline' in src.lower()

    def is_pipeline(p, c):
        return p in PIPELINE_PKGS or c.split('.')[0].startswith('Pipeline')

    hit = {t for t in members
           if f'{t[0]}.{t[1]}' in aliased
           or ((has_pipeline or not is_pipeline(t[0], t[1])) and used(src, t[1], t[2], t[3]))}
    return members, hit


def update_readme(results):
    path = os.path.join(ROOT, 'README.md')
    s = open(path).read()
    for moddir, pct in results.items():
        color = 'green' if pct >= 80 else 'orange'
        pat = re.compile(r'\[!\[\d+%\]\(https://img\.shields\.io/badge/-\d+%25-\w+\?style=flat-square\)\](\(/' + moddir + r'/)')
        s, n = pat.subn(lambda m: f'[![{pct}%](https://img.shields.io/badge/-{pct}%25-{color}?style=flat-square)]{m.group(1)}', s)
        if n != 1:
            sys.exit(f'expected exactly one badge for {moddir} in README.md, found {n}')
    open(path, 'w').write(s)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--write', action='store_true', help='update the badges in README.md')
    ap.add_argument('-v', '--verbose', action='store_true', help='list used and missing members per class')
    ap.add_argument('--exclude-pipeline', action='store_true', help='ignore the Firestore Pipeline API')
    ap.add_argument('--api-dir', help='directory of local <module>.txt api files instead of downloading')
    ap.add_argument('modules', nargs='*', help='subset of modules (default: all)')
    args = ap.parse_args()

    results = {}
    for api, moddir in MODULES.items():
        if args.modules and api not in args.modules:
            continue
        members, hit = coverage(api, moddir, fetch_api(api, args.api_dir), args.exclude_pipeline)
        pct = int(100 * len(hit) / len(members) + 0.5)  # round half up
        results[moddir] = pct
        print(f'{moddir:22s} {len(hit):4d}/{len(members):4d} = {pct:3d}%')
        if args.verbose:
            byc = collections.defaultdict(lambda: ([], []))
            for t in members:
                byc[t[1]][t in hit].append(t[3])
            for c, (miss, ok) in byc.items():
                print(f'   {c}: used={ok}\n      missing={miss}')
    if args.write:
        update_readme(results)
        print('README.md updated')


if __name__ == '__main__':
    main()
