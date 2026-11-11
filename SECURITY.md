# Security Policy

This policy covers rulii and the libraries built on it: rulii-spring, rulii-bean-validation, rulii-explorer and
rulii-bom.

## Supported versions

Security fixes go into the latest release of each library. Older release lines don't get them, so the fix for a
problem in an old version is to upgrade.

## Reporting a vulnerability

Please don't open a public issue or discussion for a security problem.

Report it privately, either way:

- [Report a vulnerability](https://github.com/algox/rulii/security/advisories/new) on GitHub, which opens a private
  advisory only the maintainer can see.
- Email info@rulii.org.

It helps to include:

- the library and version
- what happens, and what you expected
- the smallest rule, rule set or configuration that shows it
- what someone could do with it

You'll get a reply within five working days. If the report holds up, I'll agree a fix and a disclosure date with you,
publish a release, and credit you in the advisory unless you'd rather not be named.

## What counts

rulii runs code you give it, and some of that is by design. Before you report, these are the lines it draws.

**Rules, scripts and rule XML are code.** A script rule (JavaScript, Groovy, Jakarta EL, SpEL, Java through Janino),
an `@Expr` condition, a message template and a rule declared in XML all run with the application's own permissions.
They are as trusted as the classes next to them. An application that builds any of them from user input has an
injection problem of its own making, and rulii doesn't sandbox it.

**The explorer shows your rules.** rulii-explorer is off by default and sits behind Spring Boot Actuator. Once it's
switched on, whoever can reach the endpoint can read every rule's name, description and source location. Protect it
the way you protect your other Actuator endpoints.

What I'd want to hear about: a way to run code or read data that the application's author didn't put in a rule, or
anything in the explorer that shows data to someone the application's own security would have kept out.
