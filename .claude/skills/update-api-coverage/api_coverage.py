#!/usr/bin/env python3
"""Recalculate the "API Coverage" badges in README.md.

Coverage for a module is the number of public, non-deprecated firebase-android-sdk
API members that this SDK's androidMain sources invoke, divided by the total
number of such members. The Android API comes from the module's api.txt on the
main branch of firebase-android-sdk, or, for the closed-source Authentication and
Analytics libraries, from `javap -v` over the AARs the Firebase BOM resolves to.

Usage (from the repository root):
    python3 .claude/skills/update-api-coverage/api_coverage.py            # print the table
    python3 .claude/skills/update-api-coverage/api_coverage.py --write    # also update README.md
    python3 .claude/skills/update-api-coverage/api_coverage.py -v         # per-class detail
    python3 .claude/skills/update-api-coverage/api_coverage.py --exclude-pipeline
    python3 .claude/skills/update-api-coverage/api_coverage.py --api-dir DIR  # use local api.txt files
    python3 .claude/skills/update-api-coverage/api_coverage.py --aar-dir DIR  # use local AAR files
"""
import argparse
import collections
import glob
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.request
import zipfile

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..'))
API_URL = 'https://raw.githubusercontent.com/firebase/firebase-android-sdk/main/firebase-{}/api.txt'
MAVEN_URL = 'https://dl.google.com/dl/android/maven2/'

# Modules whose Android API is published as api.txt in firebase-android-sdk.
API_MODULES = ['database', 'firestore', 'functions', 'messaging', 'storage',
               'installations', 'config', 'perf', 'crashlytics']
# Closed-source modules: (Maven artifact holding the classes, Java package).
# The firebase-analytics AAR is an empty shim; its API lives in play-services-measurement-api,
# whose version is a dependency of the firebase-analytics POM.
AAR_MODULES = {
    'auth': ('com.google.firebase:firebase-auth', 'com.google.firebase.auth'),
    'analytics': ('com.google.android.gms:play-services-measurement-api', 'com.google.firebase.analytics'),
}
MODULES = {m: f'firebase-{m}' for m in API_MODULES + list(AAR_MODULES)}
EXCLUDED_SUBPACKAGES = ('internal', 'connector')
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


# --- closed-source libraries: list the API from the AAR with javap -------------------------

def fetch(url):
    with urllib.request.urlopen(url) as r:
        return r.read()


def maven_path(coordinate, version, ext):
    group, artifact = coordinate.split(':')
    return f"{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}.{ext}"


def pom_dependency_version(pom, artifact):
    m = re.search(r'<artifactId>' + re.escape(artifact) + r'</artifactId>\s*<version>([^<]+)</version>', pom)
    if not m:
        sys.exit(f'{artifact} not found in POM')
    return m.group(1)


def bom_version():
    toml = open(os.path.join(ROOT, 'gradle', 'libs.versions.toml')).read()
    return re.search(r'^firebase-bom\s*=\s*"([^"]+)"', toml, flags=re.M).group(1)


def download_aar(api, workdir):
    """Resolve the artifact version through the Firebase BOM and download its AAR."""
    bom = fetch(MAVEN_URL + maven_path('com.google.firebase:firebase-bom', bom_version(), 'pom')).decode()
    coordinate, _ = AAR_MODULES[api]
    if api == 'analytics':
        analytics_pom = fetch(MAVEN_URL + maven_path('com.google.firebase:firebase-analytics',
                                                     pom_dependency_version(bom, 'firebase-analytics'), 'pom')).decode()
        version = pom_dependency_version(analytics_pom, 'play-services-measurement-api')
    else:
        version = pom_dependency_version(bom, coordinate.split(':')[1])
    path = os.path.join(workdir, f'{api}.aar')
    open(path, 'wb').write(fetch(MAVEN_URL + maven_path(coordinate, version, 'aar')))
    print(f'  {coordinate}:{version}', file=sys.stderr)
    return path


def find_local_aar(api, aar_dir):
    artifact = AAR_MODULES[api][0].split(':')[1]
    matches = sorted(glob.glob(os.path.join(aar_dir, f'{artifact}-*.aar')))
    if not matches:
        sys.exit(f'no {artifact}-*.aar in {aar_dir}')
    return matches[-1]


def javap_classes(aar_path, package, workdir):
    """Return `javap -v` output for every class in the package (and non-excluded subpackages)."""
    with zipfile.ZipFile(aar_path) as aar:
        jar_path = os.path.join(workdir, os.path.basename(aar_path) + '.classes.jar')
        open(jar_path, 'wb').write(aar.read('classes.jar'))
    prefix = package.replace('.', '/') + '/'
    with zipfile.ZipFile(jar_path) as jar:
        names = []
        for n in jar.namelist():
            if not (n.startswith(prefix) and n.endswith('.class')):
                continue
            rel = n[len(prefix):-len('.class')]
            sub = rel.split('/')[:-1]
            if sub and sub[0] in EXCLUDED_SUBPACKAGES:
                continue
            names.append(n[:-len('.class')].replace('/', '.'))
    if not names:
        sys.exit(f'no classes under {package} in {aar_path}')
    out = subprocess.run(['javap', '-v', '-classpath', jar_path] + names, capture_output=True, text=True)
    if out.returncode:
        sys.exit(out.stderr)
    return out.stdout


def parse_javap(output, package):
    """Yield (package, class, kind, name) for every public, non-deprecated member, matching parse_api."""
    def deprecated(attrs):
        return any(re.search(r'Deprecated: true|(java/lang|kotlin)/Deprecated', a) for a in attrs)

    for chunk in re.split(r'^Classfile ', output, flags=re.M)[1:]:
        m = re.search(r'^(?:[\w ]* )?(?:class|interface|enum|@interface) ([\w.$]+)', chunk, flags=re.M)
        if not m:
            continue
        fqn = m.group(1)
        cls_flags = re.search(r'^  flags: .*$', chunk, flags=re.M).group(0)
        head, _, rest = chunk.partition('\n{\n')
        body, _, tail = rest.partition('\n}\n')
        simple = fqn[len(fqn.rsplit('.', 1)[0]) + 1:]
        if ('ACC_PUBLIC' not in cls_flags or 'ACC_SYNTHETIC' in cls_flags
                or re.search(r'\$\d|\$\$|(^|\$)zz', simple) or deprecated([tail])
                or 'kotlin/Metadata' in tail and 'k=I3' in tail):  # k=3 is a synthetic Kotlin class
            continue
        pkg = fqn.rsplit('.', 1)[0]
        cls = simple.replace('$', '.')
        is_enum = 'ACC_ENUM' in cls_flags

        member = None
        attrs = []
        members = []
        for line in body.split('\n') + ['  ']:
            if line.startswith('  ') and not line.startswith('    '):
                if member:
                    members.append((member, attrs))
                member, attrs = line.strip(), []
            elif member is not None:
                attrs.append(line)
        for decl, attrs in members:
            flags = next((a for a in attrs if a.strip().startswith('flags:')), '')
            if ('ACC_PUBLIC' not in flags or 'ACC_SYNTHETIC' in flags or 'ACC_BRIDGE' in flags
                    or deprecated(attrs) or decl.startswith('static {}')):
                continue
            if '(' in decl:
                name = decl.split('(')[0].split()[-1]
                if name.replace('$', '.') == fqn.replace('$', '.') or name == simple:
                    kind, name = 'ctor', '<init>'
                else:
                    kind = 'method'
                    if is_enum and name in ('values', 'valueOf'):
                        continue
            else:
                name = decl.rstrip(';').split()[-1]
                kind = 'enum_constant' if 'ACC_ENUM' in flags else 'field'
            if '$' in name or name.startswith('zz') or name in ('Companion', 'INSTANCE'):
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


def coverage(api, moddir, members, exclude_pipeline):
    members = sorted(set(members))
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
        color = 'green' if pct > 60 else 'orange'
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
    ap.add_argument('--aar-dir', help='directory of local firebase-auth-*.aar / play-services-measurement-api-*.aar '
                                      'files instead of downloading them from Google Maven')
    ap.add_argument('modules', nargs='*', help='subset of modules (default: all)')
    args = ap.parse_args()

    results = {}
    workdir = tempfile.mkdtemp(prefix='api-coverage-')
    for api, moddir in MODULES.items():
        if args.modules and api not in args.modules:
            continue
        if api in AAR_MODULES:
            if not shutil.which('javap'):
                sys.exit('javap (JDK) is required for the Authentication and Analytics badges')
            aar = find_local_aar(api, args.aar_dir) if args.aar_dir else download_aar(api, workdir)
            package = AAR_MODULES[api][1]
            members = parse_javap(javap_classes(aar, package, workdir), package)
        else:
            members = parse_api(fetch_api(api, args.api_dir), PIPELINE_PKGS if args.exclude_pipeline else ())
        members, hit = coverage(api, moddir, members, args.exclude_pipeline)
        pct = int(100 * len(hit) / len(members) + 0.5)  # round half up
        results[moddir] = pct
        print(f'{moddir:22s} {len(hit):4d}/{len(members):4d} = {pct:3d}%')
        if args.verbose:
            byc = collections.defaultdict(lambda: ([], []))
            for t in members:
                byc[t[1]][t in hit].append(t[3])
            for c, (miss, ok) in byc.items():
                print(f'   {c}: used={ok}\n      missing={miss}')
    shutil.rmtree(workdir, ignore_errors=True)
    if args.write:
        update_readme(results)
        print('README.md updated')


if __name__ == '__main__':
    main()
