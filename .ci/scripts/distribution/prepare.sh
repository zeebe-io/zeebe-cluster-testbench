#!/bin/sh -eux

apt-get -qq update
apt-get install --no-install-recommends -qq -y jq libatomic1 bats git wget

# install shell check
shellcheckVersion=v0.7.1
wget "https://github.com/koalaman/shellcheck/releases/download/$shellcheckVersion/shellcheck-$shellcheckVersion.linux.x86_64.tar.xz"
tar -xvf "shellcheck-$shellcheckVersion.linux.x86_64.tar.xz"
cp "shellcheck-$shellcheckVersion"/shellcheck /usr/bin/shellcheck

# install bats
git clone https://github.com/bats-core/bats-core.git
cd bats-core
./install.sh /usr/local
