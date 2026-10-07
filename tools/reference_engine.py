"""Reference oracle for Ledgr engine logic. Amounts are integer paise.
Run: python3 reference_engine.py   -> prints golden values used in docs/GOLDEN_SCENARIOS.md
The Kotlin :engine must match these outputs exactly."""
import hashlib

def rotation(tie_key: str) -> int:
    return int.from_bytes(hashlib.sha256(tie_key.encode()).digest()[:4], "big")

def allocate(total, weights, tie_key):
    n, sw = len(weights), sum(weights)
    assert sw > 0
    sign = -1 if total < 0 else 1
    t = abs(total)
    base = [t * w // sw for w in weights]
    rem = [(t * w) % sw for w in weights]
    leftover = t - sum(base)
    rot = rotation(tie_key) % n
    order = sorted(range(n), key=lambda i: (-rem[i], (i - rot) % n))
    for i in order[:leftover]:
        base[i] += 1
    return [sign * b for b in base]

def nets(expenses, settlements=()):
    """expenses: list of (paid{p:amt}, shares{p:amt}); settlements: (from,to,amt)."""
    net = {}
    for paid, shares in expenses:
        for p, a in paid.items(): net[p] = net.get(p, 0) + a
        for p, a in shares.items(): net[p] = net.get(p, 0) - a
    for f, t, a in settlements:
        net[f] = net.get(f, 0) + a
        net[t] = net.get(t, 0) - a
    return net

def simplify(net):
    cred = {p: a for p, a in net.items() if a > 0}
    debt = {p: -a for p, a in net.items() if a < 0}
    out = []
    while cred and debt:
        c = sorted(cred.items(), key=lambda x: (-x[1], x[0]))[0]
        d = sorted(debt.items(), key=lambda x: (-x[1], x[0]))[0]
        x = min(c[1], d[1])
        out.append((d[0], c[0], x))
        cred[c[0]] -= x; debt[d[0]] -= x
        if cred[c[0]] == 0: del cred[c[0]]
        if debt[d[0]] == 0: del debt[d[0]]
    return out

def eq(total, people, key):
    return dict(zip(people, allocate(total, [1] * len(people), key)))

R = 100  # rupees -> paise
if __name__ == "__main__":
    # G1 Goa trip
    A, Ra, Am, Ro, S = "Abhi", "Rahul", "Aman", "Rohit", "Sameer"
    five = [A, Ra, Am, Ro, S]
    ex = [
        ({A: 6000*R}, eq(6000*R, five, "g1-e1")),
        ({Ra: 2400*R}, eq(2400*R, five, "g1-e2")),
        ({A: 3000*R}, eq(3000*R, [A, Ra, Am, Ro], "g1-e3")),
        ({Am: 1500*R}, eq(1500*R, five, "g1-e4")),
        ({Ro: 800*R}, {A: 200*R, Ro: 300*R, S: 300*R}),
    ]
    n = nets(ex); print("G1 nets", n, "sum", sum(n.values()))
    print("G1 simplified", simplify(n))
    n2 = nets(ex, [(S, A, 1000*R)]); print("G1 after Sameer pays 1000", n2, simplify(n2))
    # G2 cycle
    ex2 = [({Ra: 1000*R}, eq(1000*R, [A, Ra], "g2-1")),
           ({Am: 1400*R}, eq(1400*R, [Am, Ra], "g2-2")),
           ({A: 600*R}, eq(600*R, [A, Am], "g2-3"))]
    n = nets(ex2); print("G2 nets", n, simplify(n))
    # G3 rounding
    for k in ["g3-a", "g3-b", "g3-c", "g3-d"]:
        print("G3 100/3", k, allocate(100*R, [1, 1, 1], k))
    print("G3 10.00/3", allocate(1000, [1, 1, 1], "g3-a"))
    # G4 itemized
    items = {A: 300*R, Ra: 200*R, Am: 500*R}
    extras = 100*R - 50*R
    ex_sh = allocate(extras, list(items.values()), "g4:extras")
    shares = {p: items[p] + e for p, e in zip(items, ex_sh)}
    print("G4 shares", shares, sum(shares.values()))
    print("G4 remainder case", allocate(1001, [3, 2, 5], "g4-r"))
    # G5 multi payer
    sh = eq(1200*R, [A, Ra, Am], "g5")
    n = nets([({A: 700*R, Ra: 500*R}, sh)]); print("G5", n, simplify(n))
    # G6 edit after partial settlement
    four = [A, Ra, Am, Ro]
    n = nets([({A: 2000*R}, eq(2000*R, four, "g6"))], [(Ra, A, 300*R)]); print("G6 before edit", n)
    n = nets([({A: 1800*R}, eq(1800*R, four, "g6b"))], [(Ra, A, 300*R)]); print("G6 after edit", n, sum(n.values()))
    n = nets([({A: 2000*R}, eq(2000*R, four, "g6"))]); print("G7 settlement voided", n)
    # G8 refund
    orig = ({A: 2000*R}, eq(2000*R, four, "g8"))
    refund = ({A: -400*R}, {Ra: -200*R, Am: -200*R})
    n = nets([orig, refund]); print("G8", n, sum(n.values()))
    # G10 overpay
    n = nets([({A: 2000*R}, eq(2000*R, four, "g6"))], [(Ra, A, 700*R)]); print("G10", n)
    # G15 safe to spend
    print("G15", (9000*R - 3500*R - 500*R) // 26)
